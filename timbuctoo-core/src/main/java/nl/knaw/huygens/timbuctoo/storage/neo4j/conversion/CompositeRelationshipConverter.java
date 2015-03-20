package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipConverter;

import org.neo4j.graphdb.Relationship;

class CompositeRelationshipConverter<T extends Relation> implements RelationshipConverter<T> {

  private List<RelationshipConverter<? super T>> converters;

  public CompositeRelationshipConverter(List<RelationshipConverter<? super T>> converters) {
    this.converters = converters;
  }

  @Override
  public void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException {
    executeAction(new ActionExecutor<T>() {
      @Override
      public void execute(RelationshipConverter<? super T> converter, Relationship relationship, T entity) throws ConversionException {
        converter.addValuesToPropertyContainer(relationship, entity);
      }
    }, relationship, entity);
  }

  @Override
  public void addValuesToEntity(T entity, Relationship relationship) throws ConversionException {
    executeAction(new ActionExecutor<T>() {
      @Override
      public void execute(RelationshipConverter<? super T> converter, Relationship relationship, T entity) throws ConversionException {
        converter.addValuesToEntity(entity, relationship);
      }
    }, relationship, entity);
  }

  @Override
  public void updatePropertyContainer(Relationship relationship, T entity) throws ConversionException {
    executeAction(new ActionExecutor<T>() {
      @Override
      public void execute(RelationshipConverter<? super T> converter, Relationship relationship, T entity) throws ConversionException {
        converter.updatePropertyContainer(relationship, entity);
      }
    }, relationship, entity);
  }

  @Override
  public void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException {
    executeAction(new ActionExecutor<T>() {
      @Override
      public void execute(RelationshipConverter<? super T> converter, Relationship relationship, T entity) throws ConversionException {
        converter.updateModifiedAndRev(relationship, entity);
      }
    }, relationship, entity);
  }

  public List<RelationshipConverter<? super T>> getNodeConverters() {
    return this.converters;
  }

  private void executeAction(ActionExecutor<T> actionExecutor, Relationship relationship, T entity) throws ConversionException {
    for (RelationshipConverter<? super T> converter : converters) {
      actionExecutor.execute(converter, relationship, entity);
    }
  }

  private interface ActionExecutor<T> {
    void execute(RelationshipConverter<? super T> converter, Relationship relationship, T entity) throws ConversionException;
  }

}
