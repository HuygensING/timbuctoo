package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.coxautodev.graphql.tools.SchemaObjects;
import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.DataSetMetadataResolver;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.QueryType;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.UserResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;

public class RootQuery implements Supplier<GraphQLSchema> {

  private final DataSetRepository dataSetRepository;
  private final GraphQlService graphQlService;
  private final SupportedExportFormats supportedFormats;
  private GraphQLSchema graphQlSchema;

  public RootQuery(DataSetRepository dataSetRepository, GraphQlService graphQlService,
                   SupportedExportFormats supportedFormats) throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.graphQlService = graphQlService;
    this.supportedFormats = supportedFormats;
  }

  public synchronized void rebuildSchema() {
    String preamble = "interface Value {\n" +
      "  value: String!\n" +
      "  type: String!\n" +
      "}\n" +
      "\n" +
      "interface Entity {\n" +
      "  uri: String!\n" +
      "}\n" +
      "\n" +
      "schema {\n" +
      "  query: Query\n" +
      "}\n";

    final graphql.schema.idl.SchemaParser schemaParser = new graphql.schema.idl.SchemaParser();
    final TypeDefinitionRegistry registry = schemaParser.parse(preamble);
    final RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    wiring.type("Query", builder -> builder.defaultDataFetcher(dataFetchingEnvironment -> ""));
    StringBuilder root = new StringBuilder("type Query {\n");

    dataSetRepository.getDataSets().values().stream().flatMap(Collection::stream).forEach(promotedDataSet -> {
      final String name = promotedDataSet.getOwnerId() + "_" + promotedDataSet.getDataSetId();
      root.append("  ").append(name).append(":").append(name).append(" @dataSet(userId:\"\", dataSetId:\"\")\n");
      registry.merge(graphQlService.createSchema(
        promotedDataSet.getOwnerId(),
        promotedDataSet.getDataSetId(),
        name,
        dataSetRepository.getDataSet(
          promotedDataSet.getOwnerId(),
          promotedDataSet.getDataSetId()
        ).get(),
        wiring
      ));
    });
    root.append("}\n\n");

    registry.merge(schemaParser.parse(root.toString()));

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    graphQlSchema = schemaGenerator.makeExecutableSchema(registry, wiring.build());
  }

  public SchemaObjects makeManualSchema() {
    return SchemaParser.newParser()
      .schemaString(
        "schema {\n" +
          "  query: StaticQueryType\n" +
          "}\n" +
          "\n" +
          "type StaticQueryType {\n" +
          "  #the datasets that are supposed to get extra attention\n" +
          "  promotedDataSets: [DataSetMetadata!]!\n" +
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
          "  title: String!\n" +
          "  description: String\n" +
          "  imageUrl: String\n" +
          "  owner: ContactInfo!\n" +
          "  contact: ContactInfo!\n" +
          "  provenanceInfo: ProvenanceInfo!\n" +
          "  license: License! \n" +
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
          "  uri: String @fake(type: url)\n" +
          "}\n" +
          "\n" +
          "type ProvenanceInfo {\n" +
          "  title: String!\n" +
          "  body: String!\n" +
          "}"
      )
      .resolvers(
        new QueryType(dataSetRepository, supportedFormats),
        new DataSetMetadataResolver(dataSetRepository),
        new UserResolver(dataSetRepository)
      )
      .build()
      .parseSchemaObjects();
  }

  @Override
  public GraphQLSchema get() {
    rebuildSchema();
    return graphQlSchema;
  }
}
