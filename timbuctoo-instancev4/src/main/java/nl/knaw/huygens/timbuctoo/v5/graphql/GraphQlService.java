package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.GraphQlTypesContainer.ENTITY_INTERFACE_NAME;
import static nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.GraphQlTypesContainer.VALUE_INTERFACE_NAME;

public class GraphQlService {
  private final DataSetRepository dataSetRepository;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final RdfWiringFactory wiringFactory;

  public GraphQlService(DataSetRepository dataSetRepository, DerivedSchemaTypeGenerator typeGenerator,
                        RdfWiringFactory wiringFactory) {
    this.dataSetRepository = dataSetRepository;
    this.typeGenerator = typeGenerator;

    this.wiringFactory = wiringFactory;
  }

  private Optional<GraphQL> loadSchema(String userId, String dataSetName) {
    return dataSetRepository
      .getDataSet(userId, dataSetName)
      .map(dataSet -> {
        final RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
        runtimeWiring.wiringFactory(wiringFactory);

        TypeNameStore typeNameStore = dataSet.getTypeNameStore();
        SchemaStore schemaStore = dataSet.getSchemaStore();

        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry registry = schemaParser.parse(typeGenerator.makeGraphQlTypes(
          "Query",
          schemaStore.getTypes(),
          typeNameStore
        ));

        SchemaGenerator schemaGenerator = new SchemaGenerator();

        String preamble = "interface " + VALUE_INTERFACE_NAME + " {\n" +
          "  value: String!\n" +
          "  type: String!\n" +
          "}\n" +
          "\n" +
          "interface " + ENTITY_INTERFACE_NAME + " {\n" +
          "  uri: String!\n" +
          "}\n" +
          "\n" +
          "schema {\n" +
          "  query: Query\n" +
          "}\n";

        registry.merge(schemaParser.parse(preamble));
        final GraphQLSchema schema = schemaGenerator.makeExecutableSchema(
          registry,
          runtimeWiring.build()
        );
        return GraphQL
          .newGraphQL(schema)
          .queryExecutionStrategy(new SerializerExecutionStrategy())
          .build();
      });
  }

  public Optional<SerializableResult> executeQuery(String userId, String dataSet, String query)
    throws GraphQlProcessingException, GraphQlFailedException {
    try {
      Optional<GraphQL> graphQl = loadSchema(userId, dataSet);
      if (graphQl.isPresent()) {
        ExecutionResult result = graphQl.get().execute(builder -> builder
          .root((DatabaseResult) () -> dataSetRepository.getDataSet(userId, dataSet).get())
          .query(query)
        );
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
