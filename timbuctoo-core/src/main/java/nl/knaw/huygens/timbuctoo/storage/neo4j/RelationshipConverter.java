package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Relationship;

public interface RelationshipConverter<T extends Relation> extends PropertyContainerConverter<Relationship, T> {

  @Override
  void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException;

  @Override
  void addValuesToEntity(T entity, Relationship relationship) throws ConversionException;

  @Override
  void updatePropertyContainer(Relationship relationship, T entity) throws ConversionException;

  @Override
  void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException;

}