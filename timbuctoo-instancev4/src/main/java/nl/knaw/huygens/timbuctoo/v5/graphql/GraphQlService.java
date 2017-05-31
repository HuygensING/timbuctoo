package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.datastores.CachedDataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexSchemaFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
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
  private final CollectionIndexSchemaFactory schemaFactory;
  private final CachedDataStoreFactory<SchemaStore> schemaStoreFactory;
  private final CachedDataStoreFactory<TypeNameStore> typeNameStoreFactory;
  private final CachedDataStoreFactory<? extends DataFetcherFactory> dataFetcherFactoryFactory;
  private final GraphQlTypeGenerator typeGenerator;

  public GraphQlService(CachedDataStoreFactory<SchemaStore> schemaStoreFactory,
                        CachedDataStoreFactory<TypeNameStore> typeNameStoreFactory,
                        CachedDataStoreFactory<? extends DataFetcherFactory> dataFetcherFactoryFactory,
                        GraphQlTypeGenerator typeGenerator) {
    this.schemaStoreFactory = schemaStoreFactory;
    this.typeNameStoreFactory = typeNameStoreFactory;
    this.dataFetcherFactoryFactory = dataFetcherFactoryFactory;
    this.typeGenerator = typeGenerator;
    this.schemaFactory = new CollectionIndexSchemaFactory();
  }

  public GraphQL loadSchema(String userId, String dataSetName) throws GraphQlProcessingException {
    try {
      PaginationArgumentsHelper paginationArgumentsHelper = new PaginationArgumentsHelper();
      Map<String, GraphQLObjectType> graphQlTypes = typeGenerator.makeGraphQlTypes(
        schemaStoreFactory.getOrCreate(userId, dataSetName).getTypes(),
        typeNameStoreFactory.getOrCreate(userId, dataSetName),
        dataFetcherFactoryFactory.getOrCreate(userId, dataSetName),
        paginationArgumentsHelper
      );

      return GraphQL
        .newGraphQL(
          newSchema()
            .query(schemaFactory
              .createQuerySchema(
                graphQlTypes,
                dataFetcherFactoryFactory.getOrCreate(userId, dataSetName),
                paginationArgumentsHelper
              )
            )
            .build()
        )
        .queryExecutionStrategy(new SerializerExecutionStrategy(typeNameStoreFactory.getOrCreate(userId, dataSetName)))
        .build();
    } catch (DataStoreCreationException e) {
      throw new GraphQlProcessingException(e);
    }
  }

  public Serializable executeQuery(String userId, String dataSet, String query)
      throws GraphQlProcessingException, GraphQlFailedException {
    try {
      GraphQL graphQl;
      if (graphQls.containsKey(dataSet)) {
        graphQl = graphQls.get(dataSet);
      } else {
        graphQl = loadSchema(userId, dataSet);
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
