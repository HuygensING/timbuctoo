package nl.knaw.huygens.timbuctoo.v5.graphql.serializable;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionParameters;
import graphql.execution.NonNullableFieldWasNullException;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Entity;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.PredicateInfo;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.QueryContainer;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator.ENTITY_INTERFACE_NAME;
import static nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator.VALUE_INTERFACE_NAME;
import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.GraphqlIntrospectionList.graphqlIntrospectionList;
import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.GraphqlIntrospectionObject.graphqlIntrospectionObject;
import static nl.knaw.huygens.timbuctoo.v5.serializable.dto.SerializableList.serializableList;
import static org.slf4j.LoggerFactory.getLogger;

public class SerializerExecutionStrategy extends SimpleExecutionStrategy {

  private final TypeNameStore typeNameStore;
  private static final Logger LOG = getLogger(SerializerExecutionStrategy.class);

  public SerializerExecutionStrategy(TypeNameStore typeNameStore) {
    this.typeNameStore = typeNameStore;
  }

  @Override
  public ExecutionResult execute(ExecutionContext executionContext, ExecutionParameters parameters)
      throws NonNullableFieldWasNullException {
    Map<String, java.util.List<Field>> fields = parameters.fields();
    GraphQLObjectType parentType = parameters.typeInfo().castType(GraphQLObjectType.class);

    boolean removeUri = false;
    boolean removeType = false;
    if (isEntity(parentType)) {
      removeUri = addField(fields, "uri");
      removeType = addField(fields, "__typename");
    } else if (isValue(parentType)) {
      removeType = addField(fields, "__typename");
    }

    ExecutionResult result = super.execute(executionContext, parameters);
    Map<String, Object> data  = result.getData();
    if (isValue(parentType)) {
      String value = ((TypedValue) parameters.source()).getValue();
      String typename = ((TypedValue) parameters.source()).getType().iterator().next();
      return new ExecutionResultImpl(
        value == null ? null : Value.create(value, typename),
        result.getErrors(),
        result.getExtensions()
      );
    } else if (isEntity(parentType)) {
      final String uri = data.get("uri") + "";
      final String type = typeNameStore.makeUri(data.get("__typename") + "");
      if (removeUri) {
        data.remove("uri");
      }
      if (removeType) {
        data.remove("__typename");
      }
      LinkedHashMap<PredicateInfo, Serializable> copy = new LinkedHashMap<>();
      for (Map.Entry<String, Object> entry : data.entrySet()) {
        List<Field> fieldList = fields.get(entry.getKey());
        if (fieldList.size() > 1) {
          LOG.error("More then one field in fieldList. What does that mean?");
        }
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
          copy.put(predicateInfo, (Serializable) entry.getValue());
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
          copy.put(entry.getKey(), Value.fromRawJavaType(entry.getValue()));
        }
      }

      return new ExecutionResultImpl(
        graphqlIntrospectionObject(copy),
        result.getErrors(),
        result.getExtensions()
      );
    }
  }

  @Override
  protected ExecutionResult completeValueForList(ExecutionContext executionContext, ExecutionParameters parameters,
                                                 List<Field> fields, Iterable<Object> result) {
    ExecutionResult completedResult = super.completeValueForList(executionContext, parameters, fields, result);
    if (parameters.source() instanceof PaginatedList) {
      PaginatedList source = (PaginatedList) parameters.source();
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
  }

  private boolean isEntity(GraphQLObjectType graphQlObjectType) {
    return graphQlObjectType.getInterfaces().stream().anyMatch(i -> i.getName().equals(ENTITY_INTERFACE_NAME));
  }

  private boolean isValue(GraphQLObjectType graphQlObjectType) {
    return graphQlObjectType.getInterfaces().stream().anyMatch(i -> i.getName().equals(VALUE_INTERFACE_NAME));
  }

  private boolean addField(Map<String, java.util.List<Field>> fields, String key) {
    java.util.List uriField = fields.computeIfAbsent(key, k -> new ArrayList<>());
    if (uriField.isEmpty()) {
      uriField.add(new Field(key));
      return true;
    } else {
      return false;
    }
  }

}
