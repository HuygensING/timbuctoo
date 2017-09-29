package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;

import java.util.Optional;

public class GraphQlService {
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final DataSetRepository dataSetRepository;
  private final PaginationArgumentsHelper argumentsHelper;

  public GraphQlService(DataSetRepository dataSetRepository, DerivedSchemaTypeGenerator typeGenerator,
                        Archetypes archetypes, TimbuctooRdfIdHelper rdfIdHelper) {
    this.dataSetRepository = dataSetRepository;
    this.typeGenerator = typeGenerator;

    argumentsHelper = new PaginationArgumentsHelper();
  }

  private Optional<GraphQL> loadSchema(String userId, String dataSetName) {
    return dataSetRepository
      .getDataSet(userId, dataSetName)
      .map(dataSet -> {
        final RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
        final TypeDefinitionRegistry registry = createSchema(
          userId,
          dataSetName,
          "Query",
          dataSet,
          runtimeWiring
        );

        SchemaGenerator schemaGenerator = new SchemaGenerator();

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

        final SchemaParser schemaParser = new SchemaParser();
        registry.merge(schemaParser.parse(preamble));
        final GraphQLSchema schema = schemaGenerator.makeExecutableSchema(
          registry,
          runtimeWiring.build()
        );
        return GraphQL
          .newGraphQL(schema)
          .queryExecutionStrategy(new SerializerExecutionStrategy(dataSet.getTypeNameStore()))
          .build();
      });
  }

  public TypeDefinitionRegistry createSchema(String userId, String dataSetName, String rootType, DataSet dataSet,
                                             RuntimeWiring.Builder runtimeWiring) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    SchemaStore schemaStore = dataSet.getSchemaStore();

    final TypeDefinitionRegistry typeDefinitionRegistry = typeGenerator.makeGraphQlTypes(
      userId,
      dataSetName,
      rootType,
      schemaStore.getTypes(),
      typeNameStore,
      runtimeWiring,
      dataSet.getDataFetcherFactory(),
      argumentsHelper
    );
    return typeDefinitionRegistry;
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
