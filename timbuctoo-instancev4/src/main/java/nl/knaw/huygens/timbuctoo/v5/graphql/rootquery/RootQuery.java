package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableProperty;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutablePropertyList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableStringList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.MimeTypeDescription;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.Property;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.io.Resources.getResource;

public class RootQuery implements Supplier<GraphQLSchema> {

  private final DataSetRepository dataSetRepository;
  private final SupportedExportFormats supportedFormats;
  private final RdfWiringFactory wiringFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private GraphQLSchema graphQlSchema;
  private final String staticQuery;

  public RootQuery(DataSetRepository dataSetRepository, SupportedExportFormats supportedFormats,
                   RdfWiringFactory wiringFactory, DerivedSchemaTypeGenerator typeGenerator) throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.supportedFormats = supportedFormats;
    this.wiringFactory = wiringFactory;
    this.typeGenerator = typeGenerator;
    staticQuery = Resources.toString(getResource(RootQuery.class, "schema.graphql"), Charsets.UTF_8);
  }

  public synchronized void rebuildSchema() {
    final SchemaParser schemaParser = new SchemaParser();
    final RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    final TypeDefinitionRegistry registry = schemaParser.parse(staticQuery);
    wiring.type("Query", builder -> builder
      .dataFetcher("promotedDataSets", env -> dataSetRepository.getDataSets()
        .values().stream().flatMap(Collection::stream)
        .map(p -> new DataSetWithDatabase(dataSetRepository.getDataSet(p.getOwnerId(), p.getDataSetId()).get(), p))
        .collect(Collectors.toList()))
      .dataFetcher("dataSetMetadata", env -> {
        String[] parsedId = ((String) env.getArgument("dataSetId")).split("__", 2);
        return Optional.ofNullable(dataSetRepository.getDataSets().get(parsedId[0])).map(d -> {
          for (PromotedDataSet promotedDataSet : d) {
            if (promotedDataSet.getDataSetId().equals(parsedId[1])) {
              return promotedDataSet;
            }
          }
          return null;
        });
      })
      .dataFetcher("aboutMe", env -> ((RootData) env.getRoot()).getCurrentUser().orElse(null))
      .dataFetcher("availableExportMimetypes", env -> supportedFormats.getSupportedMimeTypes().stream()
        .map(MimeTypeDescription::create)
        .collect(Collectors.toList())
      )
    );
    wiring.type("DataSetMetadata", builder -> builder
      .dataFetcher("collections", env -> getCollections(env.getSource()))
      .dataFetcher("dataSetId", env -> ((PromotedDataSet) env.getSource()).getCombinedId())
    );

    wiring.type("AboutMe", builder -> builder
      .dataFetcher("dataSets", env -> dataSetRepository.getDataSetsWithWriteAccess(((User) env.getSource()).getId()))
      .dataFetcher("id", env ->
        dataSetRepository.getDataSetsWithWriteAccess(((User) env.getSource()).getPersistentId()))
      .dataFetcher("name", env ->
        dataSetRepository.getDataSetsWithWriteAccess(((User) env.getSource()).getDisplayName()))
      .dataFetcher("personalInfo", env -> "http://example.com")
      .dataFetcher("canCreateDataSet", env -> true)
    );

    wiring.wiringFactory(wiringFactory);
    StringBuilder root = new StringBuilder("type DataSets {\n");

    dataSetRepository.getDataSets().values().stream().flatMap(Collection::stream).forEach(promotedDataSet -> {
      final String name = promotedDataSet.getCombinedId();

      root.append("  ").append(name).append(":").append(name).append(" @dataSet(userId:\"" + promotedDataSet
        .getOwnerId() + "\", dataSetId:\"" + promotedDataSet.getDataSetId() + "\")\n");

      final DataSet dataSet = dataSetRepository.getDataSet(
        promotedDataSet.getOwnerId(),
        promotedDataSet.getDataSetId()
      ).get();
      registry.merge(typeGenerator.makeGraphQlTypes(
        name,
        dataSet.getSchemaStore().getTypes(),
        dataSet.getTypeNameStore()
      ));
    });
    root.append("}\n\n");

    registry.merge(schemaParser.parse(root.toString()));

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    graphQlSchema = schemaGenerator.makeExecutableSchema(registry, wiring.build());
  }

  public CollectionMetadataList getCollections(PromotedDataSet input) {
    final DataSet dataSet = dataSetRepository.getDataSet(input.getOwnerId(), input.getDataSetId()).get();

    final TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    final List<ImmutableCollectionMetadata> colls = dataSet
      .getSchemaStore()
      .getTypes().values().stream()
      .map(x -> {
        final long occurrences = x.getOccurrences();
        final String collectionId = typeNameStore.makeGraphQlname(x.getName());
        return ImmutableCollectionMetadata.builder()
          .collectionId(collectionId)
          .collectionListId(collectionId + "List")
          .uri(x.getName())
          .total(occurrences)
          .properties(ImmutablePropertyList.builder()
            .prevCursor(Optional.empty())
            .nextCursor(Optional.empty())
            .items(() -> x.getPredicates().stream().map(pred -> {
                return (Property) ImmutableProperty.builder()
                  .density((pred.getOccurrences() * 100) / occurrences)
                  .name(typeNameStore.makeGraphQlnameForPredicate(pred.getName(), pred.getDirection()))
                  .referenceTypes(ImmutableStringList.builder()
                    .prevCursor(Optional.empty())
                    .nextCursor(Optional.empty())
                    .items(() -> pred.getReferenceTypes().stream().map(typeNameStore::makeGraphQlname).iterator())
                    .build()
                  )
                  .valueTypes(ImmutableStringList.builder()
                    .prevCursor(Optional.empty())
                    .nextCursor(Optional.empty())
                    .items(() -> pred.getValueTypes().stream().map(typeNameStore::makeGraphQlValuename).iterator())
                    .build()
                  )
                  .build();
              }
            ).iterator())
            .build())
          .build();
      })
      .collect(Collectors.toList());
    return ImmutableCollectionMetadataList.builder()
      .nextCursor(Optional.empty())
      .prevCursor(Optional.empty())
      .items(colls)
      .build();
  }

  @Override
  public GraphQLSchema get() {
    rebuildSchema();
    return graphQlSchema;
  }

  public static class RootData {
    public Optional<User> getCurrentUser() {
      return currentUser;
    }

    private final Optional<User> currentUser;

    public RootData(Optional<User> currentUser) {
      this.currentUser = currentUser;
    }
  }

  private static class DataSetWithDatabase implements DatabaseResult, PromotedDataSet {
    private final DataSet dataSet;
    private final PromotedDataSet promotedDataSet;

    @Override
    public String getDataSetId() {
      return promotedDataSet.getDataSetId();
    }

    @Override
    public String getOwnerId() {
      return promotedDataSet.getOwnerId();
    }

    @Override
    public String getBaseUri() {
      return promotedDataSet.getBaseUri();
    }

    @Override
    public String getCombinedId() {
      return promotedDataSet.getCombinedId();
    }

    @Value.Auxiliary
    public boolean isPromoted() {
      return promotedDataSet.isPromoted();
    }

    public DataSetWithDatabase(DataSet dataSet, PromotedDataSet promotedDataSet) {
      this.dataSet = dataSet;
      this.promotedDataSet = promotedDataSet;
    }

    @Override
    public DataSet getDataSet() {
      return dataSet;
    }
  }
}
