package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

public class RelationConverter<T extends Relation, U extends Relationship> implements EntityConverter<T, U> {

  private List<String> fieldsToIgnore;
  private List<FieldConverter> fieldConverters;

  public RelationConverter(List<String> fieldsToIgnore) {
    this.fieldsToIgnore = fieldsToIgnore;
    fieldConverters = Lists.newArrayList();
  }

  @Override
  public void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException {
    for (FieldConverter fieldConverter : fieldConverters) {
      if (!fieldsToIgnore.contains(fieldConverter.getName())) {
        fieldConverter.setPropertyContainerProperty(propertyContainer, entity);
      }
    }
  }

  @Override
  public void addValuesToEntity(T entity, U propertyContainer) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addFieldConverter(FieldConverter fieldConverter) {
    this.fieldConverters.add(fieldConverter);
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
