package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.StringValue;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.InterfaceWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnionWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookUpSubjectByUriFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.valueOf;

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
        final LookUpSubjectByUriFetcherWrapper lookupFetcher = new LookUpSubjectByUriFetcherWrapper(
          "uri",
          new QuadStoreLookUpSubjectByUriFetcher(),
          "" //FIXME: baseUri
        );

        final RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
        runtimeWiring.wiringFactory(new WiringFactory() {
          final UriFetcher uriFetcher = new UriFetcher();

          @Override
          public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
            return false;
          }

          @Override
          public boolean providesTypeResolver(UnionWiringEnvironment environment) {
            return false;
          }

          @Override
          public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
            return null;
          }

          @Override
          public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
            return null;
          }

          @Override
          public boolean providesDataFetcher(FieldWiringEnvironment environment) {
            return environment.getFieldDefinition().getDirective("fromCollection") != null ||
              environment.getFieldDefinition().getDirective("rdf") != null ||
              environment.getFieldDefinition().getDirective("uri") != null;
          }

          @Override
          public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
            if (environment.getFieldDefinition().getDirective("fromCollection") != null) {
              final Directive directive = environment.getFieldDefinition().getDirective("fromCollection");
              String uri = ((StringValue) directive.getArgument("uri").getValue()).getValue();
              boolean listAll = ((BooleanValue) directive.getArgument("listAll").getValue()).isValue();
              if (listAll) {
                return new CollectionFetcherWrapper(new CollectionDataFetcher(uri));
              } else {
                return lookupFetcher;
              }
            } else if (environment.getFieldDefinition().getDirective("rdf") != null) {
              final Directive directive = environment.getFieldDefinition().getDirective("rdf");
              String uri = ((StringValue) directive.getArgument("uri").getValue()).getValue();
              Direction direction = valueOf(((StringValue) directive.getArgument("direction").getValue()).getValue());
              boolean isList = ((BooleanValue) directive.getArgument("isList").getValue()).isValue();
              boolean isObject = ((BooleanValue) directive.getArgument("isObject").getValue()).isValue();
              boolean isValue = ((BooleanValue) directive.getArgument("isValue").getValue()).isValue();
              if (isObject && isValue) {
                return new DataFetcherWrapper(isList, new UnionDataFetcher(uri, direction));
              } else {
                if (isObject) {
                  return new DataFetcherWrapper(isList, new RelationDataFetcher(uri, direction));
                } else {
                  return new DataFetcherWrapper(isList, new TypedLiteralDataFetcher(uri));
                }
              }
            } else if (environment.getFieldDefinition().getDirective("uri") != null) {
              return uriFetcher;
            }
            return null;
          }

          @Override
          public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
            return new PropertyDataFetcher(environment.getFieldDefinition().getName());
          }
        });

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
      argumentsHelper
    );
    return typeDefinitionRegistry;
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
