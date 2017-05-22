package nl.knaw.huygens.timbuctoo.v5.graphql.serializable;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionParameters;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableList;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableUntypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SerializerExecutionStrategy extends SimpleExecutionStrategy {

  private final TypeNameStore typeNameStore;

  public SerializerExecutionStrategy(TypeNameStore typeNameStore) {
    this.typeNameStore = typeNameStore;
  }

  @Override
  public ExecutionResult execute(ExecutionContext executionContext, ExecutionParameters parameters) {
    Map<String, List<Field>> fields = parameters.fields();
    GraphQLObjectType parentType = parameters.typeInfo().castType(GraphQLObjectType.class);
    //Een "object" in graphql van be:
    // - an rdf subject
    // - a wrapped value type (a tuple of a value and a string signifying the type)
    //for subject we make sure that we also query for it's uri
    //afterwards we wrap objects in a SerializableObject

    final Serializable wrappedData;
    final ExecutionResult result;
    if (implementsInterface(parentType, "Entity")) {
      boolean manuallyAddedUri = addUriField(fields);
      result = super.execute(executionContext, parameters);

      String uri = getUri(result);

      if (manuallyAddedUri) {
        ((Map) result.getData()).remove("uri");
      }
      wrappedData = new SerializableObject(makeScalarsSerializable(result.getData()), uri, typeNameStore);
    } else if (implementsInterface(parentType, "Value")) {
      result = super.execute(executionContext, parameters);
      Map<String, Object> resultData = result.getData();
      wrappedData = new SerializableValue(resultData.get("value"), (String) resultData.get("type"));
    } else {
      result = super.execute(executionContext, parameters);
      wrappedData = new SerializableObject(makeScalarsSerializable(result.getData()), null, typeNameStore);
    }

    return new ExecutionResultImpl(
      wrappedData,
      result.getErrors(),
      result.getExtensions()
    );
  }

  private boolean implementsInterface(GraphQLObjectType objectType, String entity) {
    return objectType.getInterfaces().stream().anyMatch(i -> i.getName().equals(entity));
  }

  @Override
  protected ExecutionResult completeValueForList(ExecutionContext executionContext, ExecutionParameters parameters,
                                                 List<Field> fields, Iterable<Object> origResult) {
    ExecutionResult completedResult = super.completeValueForList(executionContext, parameters, fields, origResult);

    List<Object> data = completedResult.getData();
    for (int i = 0; i < data.size(); i++) {
      final Object entry = data.get(i);
      if (!(entry instanceof Serializable)) {
        data.set(i, new SerializableUntypedValue(entry));
      }
    }
    completedResult = new ExecutionResultImpl(
      new SerializableList(completedResult.getData()),
      completedResult.getErrors(),
      completedResult.getExtensions()
    );

    return completedResult;
  }


  private LinkedHashMap<String, Serializable> makeScalarsSerializable(LinkedHashMap<String, Object> data) {
    LinkedHashMap<String, Serializable> result = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      Object value = entry.getValue();
      Serializable serialized;
      if (value instanceof Serializable) {
        serialized = (Serializable) value;
      } else {
        serialized = new SerializableUntypedValue(value);
      }
      result.put(typeNameStore.makeUri(entry.getKey()), serialized);
    }
    return result;
  }

  private String getUri(ExecutionResult result) {
    final String uri;
    if (result.getErrors().isEmpty()) {
      uri = (String) ((Map) result.getData()).get("uri");
    } else {
      uri = null;
    }
    return uri;
  }

  private boolean addUriField(Map<String, List<Field>> fields) {
    boolean manuallyAddedUri = false;
    List<Field> uriField = fields.computeIfAbsent("uri", k -> new ArrayList<>());
    if (uriField.isEmpty()) {
      manuallyAddedUri = true;
      uriField.add(new Field("uri"));
    }
    return manuallyAddedUri;
  }

}
