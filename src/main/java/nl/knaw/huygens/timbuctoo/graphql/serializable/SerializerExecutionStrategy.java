package nl.knaw.huygens.timbuctoo.graphql.serializable;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.FieldValueInfo;
import graphql.execution.NonNullableFieldWasNullException;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.DirectiveRetriever;
import nl.knaw.huygens.timbuctoo.serializable.dto.Entity;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionList;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionObject;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionValue;
import nl.knaw.huygens.timbuctoo.serializable.dto.PredicateInfo;
import nl.knaw.huygens.timbuctoo.serializable.dto.QueryContainer;
import nl.knaw.huygens.timbuctoo.serializable.dto.Serializable;
import nl.knaw.huygens.timbuctoo.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.graphql.DirectiveRetriever.getDirectiveArgument;
import static nl.knaw.huygens.timbuctoo.serializable.dto.SerializableList.serializableList;
import static org.slf4j.LoggerFactory.getLogger;

public class SerializerExecutionStrategy extends AsyncExecutionStrategy {
  private static final Logger LOG = getLogger(SerializerExecutionStrategy.class);


  @Override
  public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext,
                                                    ExecutionStrategyParameters parameters)
      throws NonNullableFieldWasNullException {
    GraphQLObjectType parentType = (GraphQLObjectType) parameters.getExecutionStepInfo().getUnwrappedNonNullType();

    return super.execute(executionContext, parameters).thenApply(sourceResult -> {
      Map<String, Object> data  = sourceResult.getData();
      if (parameters.getSource() instanceof TypedValue) {
        String value = ((TypedValue) parameters.getSource()).getValue();
        String typename = ((TypedValue) parameters.getSource()).getType();
        Value result;
        if (value == null) {
          result = null;
        } else if (data.containsKey("__typename")) {
          result = Value.create(value, typename, (String) data.get("__typename"));
        } else {
          result = Value.create(value, typename);
        }
        return new ExecutionResultImpl(
          result,
          sourceResult.getErrors(),
          sourceResult.getExtensions()
        );
      } else if (parameters.getSource() instanceof SubjectReference) {
        final String uri = ((SubjectReference) parameters.getSource()).getSubjectUri();
        final Set<String> types = ((SubjectReference) parameters.getSource()).getTypes();
        final String graphqlType = DirectiveRetriever.getDirectiveArgument(parentType, "rdfType", "uri").orElse(null);
        String type;
        if (graphqlType != null && types.contains(graphqlType)) {
          type = graphqlType;
        } else {
          Optional<String> firstType = types.stream().sorted().findFirst();
          if (firstType.isPresent()) {
            type = firstType.get();
          } else {
            LOG.error("No type present on " + uri + ". Expected at least RDFS_RESOURCE");
            type = RdfConstants.RDFS_RESOURCE;
          }
        }

        LinkedHashMap<PredicateInfo, Serializable> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          final String graphqlFieldName = entry.getKey();
          final GraphQLFieldDefinition fieldDesc = parentType.getFieldDefinition(entry.getKey());
          Optional<String> predicateUri = DirectiveRetriever.getDirectiveArgument(fieldDesc, "rdf", "predicate");
          Optional<Direction> direction =
            DirectiveRetriever.getDirectiveArgument(fieldDesc, "rdf", "direction").map(Direction::valueOf);
          final PredicateInfo predicateInfo;
          predicateInfo = predicateUri
            .map(predUri -> PredicateInfo.predicateInfo(graphqlFieldName, predUri, direction.orElse(Direction.OUT)))
            .orElseGet(() -> PredicateInfo.predicateInfo(graphqlFieldName, null, Direction.OUT));

          if (entry.getValue() == null || entry.getValue() instanceof Serializable) {
            copy.put(predicateInfo, (Serializable) entry.getValue());
          } else {
            copy.put(predicateInfo, Value.fromRawJavaType(entry.getValue()));
          }
        }
        return new ExecutionResultImpl(
          Entity.entity(uri, type, copy),
          sourceResult.getErrors(),
          sourceResult.getExtensions()
        );
      } else if (parameters.getSource() instanceof PaginatedList) {
        PaginatedList<? extends DatabaseResult> source = (PaginatedList) parameters.getSource();
        return new ExecutionResultImpl(
          serializableList(
            source.getPrevCursor().orElse(null),
            source.getNextCursor().orElse(null),
            ((GraphqlIntrospectionList) data.get("items")).getItems()
          ),
          sourceResult.getErrors(),
          sourceResult.getExtensions()
        );
      } else if (executionContext.getGraphQLSchema().getQueryType() == parentType) {
        return new ExecutionResultImpl(
          QueryContainer.queryContainer(sourceResult.getData()),
          sourceResult.getErrors(),
          sourceResult.getExtensions()
        );
      } else {
        LinkedHashMap<String, Serializable> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          if (entry.getValue() == null || entry.getValue() instanceof Serializable) {
            copy.put(entry.getKey(), (Serializable) entry.getValue());
          } else {
            copy.put(entry.getKey(), GraphqlIntrospectionValue.fromRawJavaType(entry.getValue()));
          }
        }

        return new ExecutionResultImpl(
          GraphqlIntrospectionObject.graphqlIntrospectionObject(copy),
          sourceResult.getErrors(),
          sourceResult.getExtensions()
        );
      }
    });

  }

  @Override
  protected FieldValueInfo completeValueForList(ExecutionContext executionContext,
                                                ExecutionStrategyParameters parameters,
                                                Iterable<Object> result) {


    final FieldValueInfo fieldValueInfo = super.completeValueForList(executionContext, parameters, result);
    final CompletableFuture<ExecutionResult> value =
        fieldValueInfo.getFieldValue().thenApply(completedResult -> {
          List<Object> data = completedResult.getData();
          List<Serializable> copy = data.stream().map(item -> {
            if (item == null || item instanceof Serializable) {
              return ((Serializable) item);
            } else {
              return Value.fromRawJavaType(item);
            }
          }).collect(Collectors.toList());
          return new ExecutionResultImpl(
              GraphqlIntrospectionList.graphqlIntrospectionList(copy),
              completedResult.getErrors(),
              completedResult.getExtensions()
          );
        });
    return FieldValueInfo.newFieldValueInfo(fieldValueInfo.getCompleteValueType())
                         .fieldValue(value)
                         .fieldValueInfos(fieldValueInfo.getFieldValueInfos())
                         .build();
  }

}
