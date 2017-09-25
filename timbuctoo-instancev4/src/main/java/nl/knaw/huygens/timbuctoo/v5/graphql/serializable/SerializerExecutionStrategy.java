package nl.knaw.huygens.timbuctoo.v5.graphql.serializable;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.NonNullableFieldWasNullException;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Entity;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.GraphqlIntrospectionValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.PredicateInfo;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.QueryContainer;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.GraphqlIntrospectionList.graphqlIntrospectionList;
import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.GraphqlIntrospectionObject.graphqlIntrospectionObject;
import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.SerializableList.serializableList;
import static org.slf4j.LoggerFactory.getLogger;

public class SerializerExecutionStrategy extends AsyncExecutionStrategy {

  private final TypeNameStore typeNameStore;
  private static final Logger LOG = getLogger(SerializerExecutionStrategy.class);

  public SerializerExecutionStrategy(TypeNameStore typeNameStore) {
    this.typeNameStore = typeNameStore;
  }

  @Override
  public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext,
                                                    ExecutionStrategyParameters parameters)
      throws NonNullableFieldWasNullException {
    Map<String, java.util.List<Field>> fields = parameters.fields();
    GraphQLObjectType parentType = parameters.typeInfo().castType(GraphQLObjectType.class);

    return super.execute(executionContext, parameters).thenApply(result -> {
      Map<String, Object> data  = result.getData();
      if (parameters.source() instanceof TypedValue) {
        String value = ((TypedValue) parameters.source()).getValue();
        String typename = ((TypedValue) parameters.source()).getType();
        return new ExecutionResultImpl(
          value == null ? null : Value.create(value, typename),
          result.getErrors(),
          result.getExtensions()
        );
      } else if (parameters.source() instanceof SubjectReference) {
        final String uri = ((SubjectReference) parameters.source()).getSubjectUri();
        final Set<String> types = ((SubjectReference) parameters.source()).getTypes();
        final String graphqlType = typeNameStore.makeUri(parentType.getName());
        String type;
        if (graphqlType != null && types.contains(graphqlType)) {
          type = graphqlType;
        } else {
          Optional<String> firstType = types.stream().sorted().findFirst();
          if (firstType.isPresent()) {
            type = firstType.get();
          } else {
            LOG.error("No type present on " + uri + ". Expected at least TIM_UNKNOWN");
            type = RdfConstants.UNKNOWN;
          }
        }

        LinkedHashMap<PredicateInfo, Serializable> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          //Graphql allows you to specify the same field under the same name more then once.
          //if the field is an object field the subqueries will be merged
          //if you use a different name then the fields will not be merged and entry.getKey() is the alias
          //
          //examples
          /*
            {
              foo { bar }
              foo { baz }
            }
            =>
            entry.getKey() == "foo"
            fieldList == [{name:"foo" selectionSet: ["bar"]}, {name:"foo" selectionSet: ["baz"]}]

            {
              foo { bar }
              bax: foo { baz }
            }
            =>
            entry.getKey() == "foo"
            fieldList == [{name:"foo" selectionSet: ["bar"]}]

            entry.getKey() == "bax"
            fieldList == [{name:"foo" selectionSet: ["baz"]}]

           */
          List<Field> fieldList = fields.get(entry.getKey());
          //so if the fieldlist contains more then one item. They will always have the same name
          String actualName = fieldList.get(0).getName();


          Optional<Tuple<String, Direction>> uriAndDirection = typeNameStore.makeUriForPredicate(actualName);
          final PredicateInfo predicateInfo;
          if (uriAndDirection.isPresent()) {
            predicateInfo = PredicateInfo.predicateInfo(
              entry.getKey(),
              uriAndDirection.get().getLeft(),
              uriAndDirection.get().getRight()
            );
          } else {
            predicateInfo = PredicateInfo.predicateInfo(
              entry.getKey(),
              null,
              Direction.OUT
            );
          }
          if (entry.getValue() == null || entry.getValue() instanceof Serializable) {
            if (entry.getValue() instanceof Value) {
              boolean typeRequested = false;
              for (Field field : fieldList) {
                for (Selection selection : field.getSelectionSet().getSelections()) {
                  if (selection instanceof Field) {
                    if (((Field) selection).getName().equals("__typename")) {
                      typeRequested = true;
                      break;
                    }
                  }
                }
              }
              final Value value = (Value) entry.getValue();
              if (typeRequested) {
                copy.put(predicateInfo, value.withGraphqlType(typeNameStore.makeGraphQlValuename(value.getType())));
              } else {
                copy.put(predicateInfo, (Serializable) entry.getValue());
              }
            } else {
              copy.put(predicateInfo, (Serializable) entry.getValue());
            }
          } else {
            copy.put(predicateInfo, Value.fromRawJavaType(entry.getValue()));
          }
        }
        return new ExecutionResultImpl(
          Entity.entity(uri, type, copy),
          result.getErrors(),
          result.getExtensions()
        );
      } else if (executionContext.getGraphQLSchema().getQueryType() == parentType) {
        return new ExecutionResultImpl(
          QueryContainer.queryContainer(result.getData()),
          result.getErrors(),
          result.getExtensions()
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
          graphqlIntrospectionObject(copy),
          result.getErrors(),
          result.getExtensions()
        );
      }
    });

  }

  @Override
  protected CompletableFuture<ExecutionResult> completeValueForList(ExecutionContext executionContext,
                                                                    ExecutionStrategyParameters parameters,
                                                                    Iterable<Object> result) {
    return super.completeValueForList(executionContext, parameters, result).thenApply(completedResult -> {
      if (parameters.source() instanceof PaginatedList) {
        PaginatedList<? extends DatabaseResult> source = (PaginatedList) parameters.source();
        return new ExecutionResultImpl(
          serializableList(
            source.getPrevCursor().orElse(null),
            source.getNextCursor().orElse(null),
            (java.util.List) completedResult.getData()
          ),
          completedResult.getErrors(),
          completedResult.getExtensions()
        );
      } else {
        List<Object> data = completedResult.getData();
        List<Serializable> copy = data.stream().map(item -> {
          if (item == null || item instanceof Serializable) {
            if (item instanceof Value) {
              boolean typeRequested = false;
              for (Field field : parameters.field()) {
                for (Selection selection : field.getSelectionSet().getSelections()) {
                  if (selection instanceof Field) {
                    if (((Field) selection).getName().equals("__typename")) {
                      typeRequested = true;
                      break;
                    }
                  }
                }
              }
              final Value value = (Value) item;
              if (typeRequested) {
                return value.withGraphqlType(typeNameStore.makeGraphQlValuename(value.getType()));
              }
            }
            return ((Serializable) item);
          } else {
            return Value.fromRawJavaType(item);
          }
        }).collect(Collectors.toList());
        return new ExecutionResultImpl(
          graphqlIntrospectionList(copy),
          completedResult.getErrors(),
          completedResult.getExtensions()
        );
      }
    });
  }

}
