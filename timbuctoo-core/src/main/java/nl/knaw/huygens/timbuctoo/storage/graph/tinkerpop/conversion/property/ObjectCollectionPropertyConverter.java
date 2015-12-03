package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.storage.FacetDeserializer;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Set;

class ObjectCollectionPropertyConverter<T> extends AbstractPropertyConverter {

  private final ObjectMapper objectMapper;
  private final Class<T> componentType;

  public ObjectCollectionPropertyConverter(Class<T> componentType) {
    this.componentType = componentType;
    objectMapper = createObjectMapper();
  }

  protected ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Facet.class, new FacetDeserializer());
    objectMapper.registerModule(module);

    return objectMapper;
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
      throw new IllegalArgumentException("Value could not be read.", e);
    }
  }

  /*
   * Jackson does not deserialize the entries of an array of complex types (non java primitives) well,
   * so we need to deserialize the the entries as an array an later covert them to the type of the field.
   */
  private <E, C extends Collection<E>> Object convertCollection(Class<E> entryType, Class<C> fieldType, String rawValue) throws IOException, IllegalAccessException, InstantiationException {
    TypeFactory typeFactory =  objectMapper.getTypeFactory();

    return objectMapper.readValue(rawValue, typeFactory.constructCollectionType(fieldType, entryType));
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
