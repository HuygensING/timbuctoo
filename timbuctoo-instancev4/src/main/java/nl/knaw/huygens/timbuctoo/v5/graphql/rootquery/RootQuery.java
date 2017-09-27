package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.coxautodev.graphql.tools.SchemaObjects;
import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.DataSetMetadataResolver;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.QueryType;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.UserResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;

public class RootQuery implements Supplier<GraphQLSchema> {

  private final DataSetRepository dataSetRepository;
  private final GraphQlService graphQlService;
  private GraphQLSchema graphQlSchema;

  public RootQuery(DataSetRepository dataSetRepository, GraphQlService graphQlService) throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.graphQlService = graphQlService;
  }

  public synchronized void rebuildSchema() {
    final GraphQLObjectType.Builder dataSets = newObject()
      .name("DataSets");
    Set<GraphQLType> types = new HashSet<>();

    final SchemaObjects manualSchema = makeManualSchema();
    types.addAll(manualSchema.getDictionary());

    dataSetRepository.getDataSets().values().stream().flatMap(Collection::stream).forEach(promotedDataSet -> {
      final GraphQlService.GeneratedSchema schema = graphQlService
        .createSchema(
          promotedDataSet.getOwnerId(),
          promotedDataSet.getDataSetId(),
          dataSetRepository.getDataSet(
            promotedDataSet.getOwnerId(),
            promotedDataSet.getDataSetId()).get()
        );
      types.addAll(schema.getAllObjectTypes());
      dataSets
        .field(newFieldDefinition()
          .name(promotedDataSet.getCombinedId())
          .type(schema.getRootObject())
          .dataFetcher(environment -> "") //dummy so that datafetching continues
          .build());
    });

    graphQlSchema = newSchema()
      .query(newObject()
        .name("Query")
        .fields(manualSchema.getQuery().getFieldDefinitions())
        .field(newFieldDefinition()
          .name("dataSets")
          .description("The auto-generated types for all datasets.")
          .type(dataSets.build())
          .dataFetcher(environment -> "") //dummy so that datafetching continues. All information has already been bound
          .build())
        .build())
      .build(types);
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
          "}\n" +
          "\n" +
          "type DataSetMetadata {\n" +
          "  dataSetId: ID!\n" +
          "  title: String!\n" +
          "  description: String\n" +
          "  imageUrl: String\n" +
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
          "}\n"
      )
      .resolvers(
        new QueryType(dataSetRepository),
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
