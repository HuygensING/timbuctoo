package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Element;

public interface ElementConverter<T extends Entity, U extends Element> {

  void updateElement(U element, Entity entity) throws ConversionException;

  void updateModifiedAndRev(U element, Entity entity) throws ConversionException;

  String getPropertyName(String fieldName);

  T convertToEntity(U element) throws ConversionException;

  void addValuesToElement(U element, T entity) throws ConversionException;

}