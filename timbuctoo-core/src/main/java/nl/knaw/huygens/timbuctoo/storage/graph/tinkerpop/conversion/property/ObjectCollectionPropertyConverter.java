package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Set;

class ObjectCollectionPropertyConverter<T> extends AbstractPropertyConverter {

  private final ObjectMapper objectMapper;
  private final Class<T> componentType;

  public ObjectCollectionPropertyConverter(Class<T> componentType) {
    this.componentType = componentType;
    objectMapper = new ObjectMapper();
  }


  @Override
  protected Object format(Object value) throws IllegalArgumentException {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Value should be a String");
    }
    try {
      return convertCollection(componentType, (Class<Collection<T>>) fieldType, value.toString());
    } catch (IOException | InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException("Value could not be read.");
    }
  }

  /*
   * Jackson does not deserialize the entries of an array of complex types (non java primitives) well,
   * so we need to deserialize the the entries as an array an later covert them to the type of the field.
   */
  private <E, C extends Collection<E>> Object convertCollection(Class<E> entryType, Class<C> fieldType, String rawValue) throws IOException, IllegalAccessException, InstantiationException {
    Class<E> arrayRep = (Class<E>) Array.newInstance(entryType, 0).getClass();

    E[] value1 = (E[]) objectMapper.readValue(rawValue, arrayRep);

    C fieldValue = instantiateCollection(entryType, fieldType);

    for (E e : value1) {
      fieldValue.add(e);
    }

    return fieldValue;
  }

  private <C extends Collection<E>, E> C instantiateCollection(Class<E> entryType, Class<C> fieldType) throws IllegalAccessException, InstantiationException {
    if (!Modifier.isAbstract(fieldType.getModifiers())) {
      return fieldType.newInstance();
    }
    else if(fieldType.isAssignableFrom(List.class)){
      return (C) Lists.newArrayList();
    }
    else if(fieldType.isAssignableFrom(Set.class)){
      return (C) Sets.newHashSet();
    }

    throw new RuntimeException("Type " + fieldType + " is not supported as field");
  }
}
