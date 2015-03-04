package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Relationship;

public class RelationConverter<T extends Relation, U extends Relationship> implements EntityConverter<T, U> {

  @Override
  public void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addValuesToEntity(T entity, U propertyContainer) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addFieldConverter(FieldConverter fieldWrapper) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updatePropertyContainer(U propertyContainer, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updateModifiedAndRev(U propertyContainer, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
