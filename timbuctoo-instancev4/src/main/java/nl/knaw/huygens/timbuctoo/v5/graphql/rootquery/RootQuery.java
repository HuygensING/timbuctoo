package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

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

public class RootQuery implements Supplier<GraphQLSchema> {

  private final DataSetRepository dataSetRepository;
  private final SupportedExportFormats supportedFormats;
  private final RdfWiringFactory wiringFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private GraphQLSchema graphQlSchema;

  public RootQuery(DataSetRepository dataSetRepository, SupportedExportFormats supportedFormats,
                   RdfWiringFactory wiringFactory, DerivedSchemaTypeGenerator typeGenerator) throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.supportedFormats = supportedFormats;
    this.wiringFactory = wiringFactory;
    this.typeGenerator = typeGenerator;
  }

  public synchronized void rebuildSchema() {
    final SchemaParser schemaParser = new SchemaParser();
    final RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    String staticQuery =
      "schema {\n" +
      "  query: Query\n" +
      "}\n" +
      "\n" +
      "interface Value {\n" +
      "  value: String!\n" +
      "  type: String!\n" +
      "}\n" +
      "\n" +
      "interface Entity {\n" +
      "  uri: String!\n" +
      "}\n" +
      "type Query {\n" +
      "  #the datasets that are supposed to get extra attention\n" +
      "  promotedDataSets: [DataSetMetadata!]!\n" +
      "\n" +
      "  #The actual dataSets\n" +
      "  dataSets: DataSets\n" +
      "\n" +
      "  #metadata for a specific dataset\n" +
      "  dataSetMetadata(dataSetId: ID): DataSetMetadata\n" +
      "\n" +
      "  #information about the logged in user, or null of no user is logged in\n" +
      "  aboutMe: AboutMe\n" +
      "\n" +
      "  #all mimetypes that you can use when downloading data from a dataSet\n" +
      "  availableExportMimetypes: [MimeType!]!\n" +
      "}\n" +
      "\n" +
      "type MimeType {\n" +
      "  name: String!\n" +
      "}\n" +
      "\n" +
      "type DataSetMetadata {\n" +
      "  dataSetId: ID!\n" +
      "  title: String @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  description: String @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  imageUrl: String @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  owner: ContactInfo @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  contact: ContactInfo @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  provenanceInfo: ProvenanceInfo @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "  license: License @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "\n" +
      "  collections(count: Int = 20, cursor: ID = \"\"): CollectionMetadataList\n" +
      "}\n" +
      "\n" +
      "type AboutMe {\n" +
      "  #datasets that this user has some specific permissions on\n" +
      "  dataSets: [DataSetMetadata!]!\n" +
      "\n" +
      "  #The unique identifier of this user\n" +
      "  id: ID!\n" +
      "\n" +
      "  #a human readable name (or empty string if not available)\n" +
      "  name: String!\n" +
      "\n" +
      "  #a url to a page with personal information on this user\n" +
      "  personalInfo: String!\n" +
      "\n" +
      "  #This user may create a new dataset on this timbuctoo instance\n" +
      "  canCreateDataSet: Boolean!\n" +
      "}\n" +
      "\n" +
      "type CollectionMetadataList {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [CollectionMetadata!]!\n" +
      "}\n" +
      "\n" +
      "type CollectionMetadata {\n" +
      "  collectionId: ID!\n" +
      "  collectionListId: ID!\n" +
      "  uri: String!\n" +
      "  title: String!\n" +
      "  archeType: String\n" +
      "  properties(count: Int = 20, cursor: ID = \"\"): PropertyList!\n" +
      "  total: Int!\n" +
      "}\n" +
      "\n" +
      "type PropertyList {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [Property!]!\n" +
      "}\n" +
      "\n" +
      "type Property {\n" +
      "  name: String\n" +
      "  density: Int\n" +
      "  referenceTypes(count: Int = 20, cursor: ID = \"\"): TypeList\n" +
      "  valueTypes(count: Int = 20, cursor: ID = \"\"): TypeList\n" +
      "}\n" +
      "\n" +
      "type TypeList {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [String!]!\n" +
      "}\n" +
      "\n" +
      "type ContactInfo {\n" +
      "  name: String!\n" +
      "  email: String\n" +
      "}\n" +
      "\n" +
      "type License {\n" +
      "  uri: String @fromRdf(uri: \"\", direction: \"OUT\", isList: false)\n" +
      "}\n" +
      "\n" +
      "type ProvenanceInfo {\n" +
      "  title: String!\n" +
      "  body: String!\n" +
      "}";

    final TypeDefinitionRegistry registry = schemaParser.parse(staticQuery);
    wiring.type("Query", builder -> builder
      .dataFetcher("promotedDataSets", env -> dataSetRepository.getDataSets()
        .values().stream().flatMap(Collection::stream)
        .map(p -> new DataSetWithDatabase(dataSetRepository.getDataSet(p.getOwnerId(), p.getDataSetId()).get(), p))
        .collect(Collectors.toList()))
      .dataFetcher("dataSets", env -> "")
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

    public String getDataSetId() {
      return promotedDataSet.getDataSetId();
    }

    public String getOwnerId() {
      return promotedDataSet.getOwnerId();
    }

    public String getCombinedId() {
      return promotedDataSet.getCombinedId();
    }

    @Value.Auxiliary
    public boolean isPromoted() {
      return promotedDataSet.isPromoted();
    }

    public static PromotedDataSet promotedDataSet(String ownerId, String dataSetId, boolean promoted) {
      return PromotedDataSet.promotedDataSet(ownerId, dataSetId, promoted);
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
