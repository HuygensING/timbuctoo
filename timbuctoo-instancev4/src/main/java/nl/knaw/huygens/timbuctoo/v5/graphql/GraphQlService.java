package nl.knaw.huygens.timbuctoo.v5.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.ArchetypesGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.CollectionIndexSchemaFactory;
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

  public Optional<GraphQL> loadSchema(String userId, String dataSetName) {
    return dataSetRepository
      .getDataSet(userId, dataSetName)
      .map(dataSet -> {
        final GeneratedSchema schema = createSchema(userId, dataSetName, dataSet);
        return GraphQL
          .newGraphQL(
            newSchema()
              .query(schema.rootObject).build(schema.allObjectTypes)
          )
          .queryExecutionStrategy(new SerializerExecutionStrategy(dataSet.getTypeNameStore()))
          .build();
      });
  }

  public GeneratedSchema createSchema(String userId, String dataSetName, DataSet dataSet) {
    PaginationArgumentsHelper paginationArgumentsHelper = new PaginationArgumentsHelper();
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

    final Set<GraphQLType> allObjectTypes = typesContainer.getAllObjectTypes();
    allObjectTypes.addAll(paginationArgumentsHelper.getListObjects());
    return new GeneratedSchema(allObjectTypes, schemaFactory
      .createQuerySchema(
        userId + "_" + dataSetName,
        typesContainer.getRdfTypeRepresentingTypes(),
        archetypesGenerator.makeGraphQlTypes(archetypes, typesContainer),
        dataFetcherFactory,
        paginationArgumentsHelper,
        rdfIdHelper.dataSet(userId, dataSetName)
      ));
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

  public static class GeneratedSchema {
    private final Set<GraphQLType> allObjectTypes;
    private final GraphQLObjectType rootObject;

    public GeneratedSchema(Set<GraphQLType> allObjectTypes, GraphQLObjectType rootObject) {
      this.allObjectTypes = allObjectTypes;
      this.rootObject = rootObject;
    }

    public Set<GraphQLType> getAllObjectTypes() {
      return allObjectTypes;
    }

    public GraphQLObjectType getRootObject() {
      return rootObject;
    }
  }

}
