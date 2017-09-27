package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLSchema;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.DataSetMetadataResolver;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.QueryType;

import java.io.IOException;
import java.util.function.Supplier;

public class RootQuery implements Supplier<GraphQLSchema> {

  private final GraphQLSchema graphQlSchema;

  public RootQuery(DataSetRepository dataSetRepository) throws IOException {
    graphQlSchema = SchemaParser.newParser()
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
      .makeExecutableSchema();
  }

  @Override
  public GraphQLSchema get() {
    return graphQlSchema;
  }
}
