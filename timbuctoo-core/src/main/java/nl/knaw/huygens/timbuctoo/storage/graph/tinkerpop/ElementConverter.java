package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.tinkerpop.blueprints.Element;

public interface ElementConverter<T extends Entity, U extends Element> {

  void updateElement(U vertex, Entity entity) throws ConversionException;

  void updateModifiedAndRev(U vertex, Entity entity) throws ConversionException;

  String getPropertyName(String fieldName);

  T convertToEntity(U vertex) throws ConversionException;

  void addValuesToElement(U vertex, T entity) throws ConversionException;

}