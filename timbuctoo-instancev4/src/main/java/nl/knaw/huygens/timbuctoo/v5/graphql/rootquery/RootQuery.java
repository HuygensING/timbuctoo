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
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableProperty;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutablePropertyList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableStringList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.MimeTypeDescription;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.Property;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.io.Resources.getResource;

public class RootQuery implements Supplier<GraphQLSchema> {

  private final DataSetRepository dataSetRepository;
  private final SupportedExportFormats supportedFormats;
  private final String archetypes;
  private final RdfWiringFactory wiringFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final SchemaParser schemaParser;
  private final String staticQuery;

  public RootQuery(DataSetRepository dataSetRepository, SupportedExportFormats supportedFormats, String archetypes,
                   RdfWiringFactory wiringFactory, DerivedSchemaTypeGenerator typeGenerator) throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.supportedFormats = supportedFormats;
    this.archetypes = archetypes;
    this.wiringFactory = wiringFactory;
    this.typeGenerator = typeGenerator;
    staticQuery = Resources.toString(getResource(RootQuery.class, "schema.graphql"), Charsets.UTF_8);
    schemaParser = new SchemaParser();
  }

  public synchronized GraphQLSchema rebuildSchema() {
    final TypeDefinitionRegistry staticQuery = schemaParser.parse(this.staticQuery);
    if (archetypes != null && !archetypes.isEmpty()) {
      staticQuery.merge(schemaParser.parse(
        archetypes +
          "extend type DataSetMetadata {\n" +
          "  archetypes: Archetypes! @passThrough\n" +
          "}\n" +
          "\n")
      );
    }
    TypeDefinitionRegistry registry = new TypeDefinitionRegistry();
    registry.merge(staticQuery);
    final RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    wiring.type("Query", builder -> builder
      .dataFetcher("promotedDataSets", env -> dataSetRepository.getPromotedDataSets()
        .values().stream().flatMap(Collection::stream)
        .map(this::makeDbResult)
        .collect(Collectors.toList()))
      .dataFetcher("allDataSets", env -> dataSetRepository.getDataSets()
        .values().stream().flatMap(Collection::stream)
        .map(this::makeDbResult)
        .collect(Collectors.toList()))
      .dataFetcher("dataSetMetadata", env -> {
        String[] parsedId = ((String) env.getArgument("dataSetId")).split("__", 2);
        return Optional.ofNullable(dataSetRepository.getDataSets().get(parsedId[0])).map(d -> {
          for (PromotedDataSet promotedDataSet : d) {
            if (promotedDataSet.getDataSetId().equals(parsedId[1])) {
              return makeDbResult(promotedDataSet);
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
      .dataFetcher("dataSets", env -> (Iterable) () -> dataSetRepository
        .getDataSetsWithWriteAccess(((User) env.getSource()).getPersistentId())
        .stream().map(this::makeDbResult).iterator()
      )
      .dataFetcher("id", env -> ((User) env.getSource()).getPersistentId())
      .dataFetcher("name", env -> ((User) env.getSource()).getDisplayName())
      .dataFetcher("personalInfo", env -> "http://example.com")
      .dataFetcher("canCreateDataSet", env -> true)
    );

    wiring.wiringFactory(wiringFactory);
    StringBuilder root = new StringBuilder("type DataSets {\n");

    boolean[] dataSetAvailable = new boolean[] {false};
    dataSetRepository.getDataSets().values().stream().flatMap(Collection::stream).forEach(promotedDataSet -> {
      final String name = promotedDataSet.getCombinedId();

      final DataSet dataSet = dataSetRepository.getDataSet(
        promotedDataSet.getOwnerId(),
        promotedDataSet.getDataSetId()
      ).get();
      final Map<String, Type> types = dataSet.getSchemaStore().getTypes();
      if (types != null) {
        dataSetAvailable[0] = true;
        root.append("  ")
          .append(name)
          .append(":")
          .append(name)
          .append(" @dataSet(userId:\"")
          .append(promotedDataSet.getOwnerId())
          .append("\", dataSetId:\"")
          .append(promotedDataSet.getDataSetId())
          .append("\")\n");

        wiring.type(name, c -> c
          .dataFetcher("metadata", env -> makeDbResult(promotedDataSet))
        );

        final String schema = typeGenerator.makeGraphQlTypes(
          name,
          types,
          dataSet.getTypeNameStore()
        );
        staticQuery.merge(schemaParser.parse(schema));
      }
    });
    root.append("}\n\nextend type Query {\n  #The actual dataSets\n  dataSets: DataSets @passThrough\n}\n\n");

    if (dataSetAvailable[0]) {
      staticQuery.merge(schemaParser.parse(root.toString()));
    }

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(staticQuery, wiring.build());
  }

  public DataSetWithDatabase makeDbResult(PromotedDataSet promotedDataSet) {
    return new DataSetWithDatabase(
      dataSetRepository.getDataSet(
        promotedDataSet.getOwnerId(),
        promotedDataSet.getDataSetId()
      ).get(),
      promotedDataSet
    );
  }

  public CollectionMetadataList getCollections(PromotedDataSet input) {
    final DataSet dataSet = dataSetRepository.getDataSet(input.getOwnerId(), input.getDataSetId()).get();

    final TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    final List<CollectionMetadata> colls = dataSet
      .getSchemaStore()
      .getTypes().values().stream()
      .map(x -> {
        final long occurrences = x.getOccurrences();
        final String collectionId = typeNameStore.makeGraphQlname(x.getName());
        return ImmutableCollectionMetadata.builder()
          .subjectUri(x.getName())
          .types(Collections.emptySet())
          .dataSet(dataSet)
          .collectionId(collectionId)
          .collectionListId(collectionId + "List")
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
    return rebuildSchema();
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

  private static class DataSetWithDatabase extends LazyTypeSubjectReference implements PromotedDataSet {
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

    public boolean isPromoted() {
      return promotedDataSet.isPromoted();
    }

    public DataSetWithDatabase(DataSet dataSet, PromotedDataSet promotedDataSet) {
      super(promotedDataSet.getBaseUri(), dataSet);
      this.promotedDataSet = promotedDataSet;
    }

    @Override
    public String getSubjectUri() {
      return promotedDataSet.getBaseUri();
    }

  }
}
