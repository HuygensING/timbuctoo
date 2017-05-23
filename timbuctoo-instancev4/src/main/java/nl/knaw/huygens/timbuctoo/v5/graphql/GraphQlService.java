package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataSetManager;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexSchemaFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQlService {
  private final Map<String, GraphQL> graphQls = new HashMap<>();
  private final DataSetManager dataSetManager;
  private final CollectionIndexSchemaFactory schemaFactory;
  private final GraphQlTypeGenerator typeGenerator;

  public GraphQlService(DataSetManager dataSetManager, GraphQlTypeGenerator typeGenerator) {
    this.dataSetManager = dataSetManager;
    this.typeGenerator = typeGenerator;
    this.schemaFactory = new CollectionIndexSchemaFactory();
  }

  public GraphQL loadSchema(String dataSetName) throws GraphQlProcessingException {
    try {
      DataStores dataStores = dataSetManager.getDataStores(dataSetName);
      Tuple<Set<GraphQLType>, Map<String, GraphQLObjectType>> graphQlTypes = typeGenerator.makeGraphQlTypes(
        dataStores.getSchemaStore().getTypes(),
        dataStores.getTypeNameStore(),
        dataStores.getDataFetcherFactory()
      );


      CollectionIndexFetcherFactory fetcherFactory = dataStores.getCollectionIndexFetcherFactory();
      return GraphQL
        .newGraphQL(
          newSchema()
            .query(schemaFactory.createQuerySchema(graphQlTypes.getRight(), fetcherFactory))
            .build(graphQlTypes.getLeft())
        )
        .queryExecutionStrategy(new SerializerExecutionStrategy(dataStores.getTypeNameStore()))
        .build();
    } catch (IOException e) {
      throw new GraphQlProcessingException(e);
    }
  }

  public Serializable executeQuery(String dataSet, String query)
      throws GraphQlProcessingException, GraphQlFailedException {
    try {
      GraphQL graphQl;
      if (graphQls.containsKey(dataSet)) {
        graphQl = graphQls.get(dataSet);
      } else {
        graphQl = loadSchema(dataSet);
        graphQls.put(dataSet, graphQl);
      }
      ExecutionResult result = graphQl.execute(query);
      if (result.getErrors().isEmpty()) {
        return result.getData();
      } else {
        throw new GraphQlFailedException(result.getErrors());
      }
    } catch (GraphQlFailedException e) {
      throw e;
    } catch (Exception e) {
      throw new GraphQlProcessingException(e);
    }
  }

}
