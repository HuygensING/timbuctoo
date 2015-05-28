package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Element;

public interface ElementConverter<T extends Entity, E extends Element> {

  void updateElement(E element, Entity entity) throws ConversionException;

  void updateModifiedAndRev(E element, Entity entity) throws ConversionException;

  String getPropertyName(String fieldName);

  T convertToEntity(E element) throws ConversionException;

  void addValuesToElement(E element, T entity) throws ConversionException;

  void removePropertyByFieldName(E element, String fieldName);

}