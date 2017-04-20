package nl.knaw.huygens.timbuctoo.v5.graphql;

import com.sleepycat.je.DatabaseException;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexSchemaFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;

import java.util.HashMap;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQlService {
  private final Map<String, GraphQL> graphQls = new HashMap<>();
  private final DataStoreFactory dataStoreFactory;
  private final CollectionIndexSchemaFactory schemaFactory;
  private final GraphQlTypeGenerator typeGenerator;

  public GraphQlService(DataStoreFactory dataStoreFactory, GraphQlTypeGenerator typeGenerator) {
    this.dataStoreFactory = dataStoreFactory;
    this.typeGenerator = typeGenerator;
    this.schemaFactory = new CollectionIndexSchemaFactory();
  }

  public GraphQL loadSchema(String dataSetName) throws GraphQlProcessingException {
    try {
      DataStores dataStores = dataStoreFactory.getDataStores(dataSetName);
      Map<String, GraphQLObjectType> graphQlTypes = typeGenerator.makeGraphQlTypes(
        dataStores.getSchemaStore().getTypes(),
        dataStores.getTypeNameStore(),
        dataStores.getDataFetcherFactory()
      );

      return GraphQL
        .newGraphQL(
          newSchema()
            .query(schemaFactory.createQuerySchema(graphQlTypes, dataStores.getCollectionIndexFetcherFactory()))
            .build()
        )
        .queryExecutionStrategy(new SerializerExecutionStrategy(dataStores.getTypeNameStore()))
        .build();
    } catch (DatabaseException e) {
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
