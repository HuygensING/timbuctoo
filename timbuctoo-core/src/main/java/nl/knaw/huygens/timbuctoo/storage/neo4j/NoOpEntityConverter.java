package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

public class NoOpEntityConverter<T extends Entity, U extends PropertyContainer> implements PropertyContainerConverter<T, U> {

  @Override
  public void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addValuesToEntity(T entity, U propertyContainer) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addFieldConverter(FieldConverter fieldWrapper) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updatePropertyContainer(U propertyContainer, Entity entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateModifiedAndRev(U propertyContainer, Entity entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

}
