package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import nl.knaw.huygens.timbuctoo.v5.archetypes.ArchetypesGenerator;
import nl.knaw.huygens.timbuctoo.v5.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexSchemaFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;

import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQlService {
  private final CollectionIndexSchemaFactory schemaFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final Archetypes archetypes;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final DataSetFactory dataSetFactory;

  public GraphQlService(DataSetFactory dataSetFactory, DerivedSchemaTypeGenerator typeGenerator,
                        Archetypes archetypes, TimbuctooRdfIdHelper rdfIdHelper) {
    this.dataSetFactory = dataSetFactory;
    this.typeGenerator = typeGenerator;
    this.archetypes = archetypes;
    this.schemaFactory = new CollectionIndexSchemaFactory();
    this.rdfIdHelper = rdfIdHelper;
  }

  public GraphQL loadSchema(String userId, String dataSetName) throws GraphQlProcessingException {
    try {
      PaginationArgumentsHelper paginationArgumentsHelper = new PaginationArgumentsHelper();
      final DataSet dataSet = dataSetFactory.createDataSet(userId, dataSetName);
      TypeNameStore typeNameStore = dataSet.getTypeNameStore();
      DataFetcherFactory dataFetcherFactory = dataSet.getDataFetcherFactory();
      SchemaStore schemaStore = dataSet.getSchemaStore();
      GraphQlTypesContainer typesContainer = new GraphQlTypesContainer(
        typeNameStore,
        dataFetcherFactory,
        paginationArgumentsHelper
      );
      ArchetypesGenerator archetypesGenerator = new ArchetypesGenerator();

      typeGenerator.makeGraphQlTypes(schemaStore.getTypes(), typeNameStore, typesContainer);

      return GraphQL
        .newGraphQL(
          newSchema()
            .query(schemaFactory
              .createQuerySchema(
                typesContainer.getRdfTypeRepresentingTypes(),
                archetypesGenerator.makeGraphQlTypes(archetypes, typesContainer),
                dataFetcherFactory,
                paginationArgumentsHelper,
                rdfIdHelper.dataSet(userId, dataSetName)
              )
            )
            .build(typesContainer.getAllObjectTypes())
        )
        .queryExecutionStrategy(new SerializerExecutionStrategy(typeNameStore))
        .build();
    } catch (DataStoreCreationException e) {
      throw new GraphQlProcessingException(e);
    }
  }

  public SerializableResult executeQuery(String userId, String dataSet, String query)
      throws GraphQlProcessingException, GraphQlFailedException {
    try {
      GraphQL graphQl = loadSchema(userId, dataSet);
      ExecutionResult result = graphQl.execute(query);
      if (result.getErrors().isEmpty()) {
        return new SerializableResult(result.getData());
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
