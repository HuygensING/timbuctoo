package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.ArchetypesGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.CollectionIndexSchemaFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.GraphQlTypesContainer;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;

import java.util.Optional;
import java.util.Set;

import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQlService {
  private final CollectionIndexSchemaFactory schemaFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final Archetypes archetypes;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final DataSetRepository dataSetRepository;

  public GraphQlService(DataSetRepository dataSetRepository, DerivedSchemaTypeGenerator typeGenerator,
                        Archetypes archetypes, TimbuctooRdfIdHelper rdfIdHelper) {
    this.dataSetRepository = dataSetRepository;
    this.typeGenerator = typeGenerator;
    this.archetypes = archetypes;
    this.schemaFactory = new CollectionIndexSchemaFactory();
    this.rdfIdHelper = rdfIdHelper;
  }

  public Optional<GraphQL> loadSchema(String userId, String dataSetName) throws GraphQlProcessingException {
    PaginationArgumentsHelper paginationArgumentsHelper = new PaginationArgumentsHelper();
    final Optional<DataSet> dataSet = dataSetRepository.getDataSet(userId, dataSetName);
    if (dataSet.isPresent()) {
      TypeNameStore typeNameStore = dataSet.get().getTypeNameStore();
      DataFetcherFactory dataFetcherFactory = dataSet.get().getDataFetcherFactory();
      SchemaStore schemaStore = dataSet.get().getSchemaStore();
      GraphQlTypesContainer typesContainer = new GraphQlTypesContainer(
        typeNameStore,
        dataFetcherFactory,
        paginationArgumentsHelper
      );
      ArchetypesGenerator archetypesGenerator = new ArchetypesGenerator();

      typeGenerator.makeGraphQlTypes(schemaStore.getTypes(), typeNameStore, typesContainer);

      final Set<GraphQLType> allObjectTypes = typesContainer.getAllObjectTypes();
      allObjectTypes.addAll(paginationArgumentsHelper.getListObjects());
      return Optional.of(GraphQL
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
            .build(allObjectTypes)
        )
        .queryExecutionStrategy(new SerializerExecutionStrategy(typeNameStore))
        .build());
    } else {
      return Optional.empty();
    }
  }

  public Optional<SerializableResult> executeQuery(String userId, String dataSet, String query)
      throws GraphQlProcessingException, GraphQlFailedException {
    try {
      Optional<GraphQL> graphQl = loadSchema(userId, dataSet);
      if (graphQl.isPresent()) {
        ExecutionResult result = graphQl.get().execute(query);
        if (result.getErrors().isEmpty()) {
          return Optional.of(new SerializableResult(result.getData()));
        } else {
          throw new GraphQlFailedException(result.getErrors());
        }
      } else {
        return Optional.empty();
      }
    } catch (GraphQlFailedException e) {
      throw e;
    } catch (Exception e) {
      throw new GraphQlProcessingException(e);
    }
  }

}
