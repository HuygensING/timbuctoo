package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.coxautodev.graphql.tools.SchemaObjects;
import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.DataSetMetadataResolver;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.QueryType;

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
    final SchemaObjects manualSchema = SchemaParser.newParser()
      .schemaString(
        "schema {\n" +
          "  query: QueryType\n" +
          "}\n" +
          "\n" +
          "type QueryType {\n" +
          "  #the datasets that are supposed to get extra attention\n" +
          "  promotedDataSets: [DataSetMetadata!]!\n" +
          "}\n" +
          "\n" +
          "type DataSetMetadata {\n" +
          "  datasetId: ID!\n" +
          "  title: String!\n" +
          "  description: String\n" +
          "  imageUrl: String\n" +
          "\n" +
          "  collections(count: Int = 20, cursor: ID = \"\"): CollectionMetadataList\n" +
          "}\n" +
          "\n" +
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
      .dictionary("DataSetMetadata", PromotedDataSet.class)
      .resolvers(
        new QueryType(dataSetRepository),
        new DataSetMetadataResolver(dataSetRepository)
      )
      .build()
      .parseSchemaObjects();
    final GraphQLObjectType.Builder dataSets = newObject()
      .name("DataSets");
    Set<GraphQLType> types = new HashSet<>();
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
        .name("Query2")
        .field(manualSchema.getQuery().getFieldDefinition("promotedDataSets"))
        .field(newFieldDefinition()
          .name("dataSets")
          .description("The auto-generated types for all datasets.")
          .type(dataSets.build())
          .dataFetcher(environment -> "") //dummy so that datafetching continues. All information has already been bound
          .build())
        .build())
      .build(types);
  }

  @Override
  public GraphQLSchema get() {
    rebuildSchema();
    return graphQlSchema;
  }
}
