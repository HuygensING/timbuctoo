package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Relationship;

public class CompositeRelationshipConverter<T extends Relation> implements RelationshipConverter<T> {

  private List<RelationshipConverter<? super T>> converters;

  public CompositeRelationshipConverter(List<RelationshipConverter<? super T>> converters) {
    this.converters = converters;
  }

  @Override
  public void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addValuesToEntity(T entity, Relationship relationship) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updatePropertyContainer(Relationship relationship, T entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  public List<RelationshipConverter<? super T>> getNodeConverters() {
    return this.converters;
  }

}
