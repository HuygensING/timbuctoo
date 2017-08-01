package nl.knaw.huygens.timbuctoo.v5.graphql.serializable;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionParameters;
import graphql.execution.NonNullableFieldWasNullException;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator.ENTITY_INTERFACE_NAME;
import static nl.knaw.huygens.timbuctoo.v5.graphql.entity.GraphQlTypeGenerator.VALUE_INTERFACE_NAME;

public class SerializerExecutionStrategy extends SimpleExecutionStrategy {

  private final TypeNameStore typeNameStore;

  public SerializerExecutionStrategy(TypeNameStore typeNameStore) {
    this.typeNameStore = typeNameStore;
  }

  @Override
  public ExecutionResult execute(ExecutionContext executionContext, ExecutionParameters parameters)
      throws NonNullableFieldWasNullException {
    Map<String, List<Field>> fields = parameters.fields();
    GraphQLObjectType parentType = parameters.typeInfo().castType(GraphQLObjectType.class);
    //Een "object" in graphql can be:
    // - an rdf subject
    // - a wrapped value type (a tuple of a value and a string signifying the type)
    //for subject we make sure that we also query for it's uri
    //afterwards we wrap objects in a SerializableObject

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
    if (data.containsKey("uri")) {
      data.put("@id", data.get("uri"));
    }
    if (data.containsKey("__typename")) {
      data.put("@type", typeNameStore.makeUri(data.get("__typename") + ""));
    }
    if (removeUri) {
      data.remove("uri");
    }
    if (removeType) {
      data.remove("__typename");
    }
    return result;
  }

  private boolean isEntity(GraphQLObjectType graphQlObjectType) {
    return graphQlObjectType.getInterfaces().stream().anyMatch(i -> i.getName().equals(ENTITY_INTERFACE_NAME));
  }

  private boolean isValue(GraphQLObjectType graphQlObjectType) {
    return graphQlObjectType.getInterfaces().stream().anyMatch(i -> i.getName().equals(VALUE_INTERFACE_NAME));
  }

  private boolean addField(Map<String, List<Field>> fields, String key) {
    List<Field> uriField = fields.computeIfAbsent(key, k -> new ArrayList<>());
    if (uriField.isEmpty()) {
      uriField.add(new Field(key));
      return true;
    } else {
      return false;
    }
  }

}
