package nl.knaw.huygens.timbuctoo.serializable.serializations;

import nl.knaw.huygens.timbuctoo.serializable.dto.Entity;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionList;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionObject;
import nl.knaw.huygens.timbuctoo.serializable.dto.GraphqlIntrospectionValue;
import nl.knaw.huygens.timbuctoo.serializable.dto.Serializable;
import nl.knaw.huygens.timbuctoo.serializable.dto.SerializableList;
import nl.knaw.huygens.timbuctoo.serializable.dto.Value;

import java.io.IOException;

public abstract class Dispatcher<T> {
  public abstract void handleEntity(Entity entity, T context) throws IOException;

  public abstract void handleNull(T context) throws IOException;

  public abstract void handleList(SerializableList list, T context) throws IOException;

  public abstract void handleGraphqlObject(GraphqlIntrospectionObject object, T context) throws IOException;

  public abstract void handleGraphqlList(GraphqlIntrospectionList list, T context) throws IOException;

  public abstract void handleGraphqlValue(GraphqlIntrospectionValue object, T context) throws IOException;

  public abstract void handleValue(Value object, T context) throws IOException;

  public void dispatch(Serializable entry, T context) throws IOException {
    switch (entry) {
      case Entity entity -> handleEntity(entity, context);
      case SerializableList serializableList -> handleList(serializableList, context);
      case Value value -> handleValue(value, context);
      case GraphqlIntrospectionObject graphqlIntrospectionObject ->
          handleGraphqlObject(graphqlIntrospectionObject, context);
      case GraphqlIntrospectionList graphqlIntrospectionList -> handleGraphqlList(graphqlIntrospectionList, context);
      case GraphqlIntrospectionValue graphqlIntrospectionValue ->
          handleGraphqlValue(graphqlIntrospectionValue, context);
      case null -> handleNull(context);
      default -> throw new IllegalStateException("Unknown value type " + entry);
    }
  }
}
