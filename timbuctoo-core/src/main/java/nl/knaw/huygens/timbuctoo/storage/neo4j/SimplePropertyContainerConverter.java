package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

public interface SimplePropertyContainerConverter<U extends PropertyContainer, T extends Entity> {

  void addFieldConverter(FieldConverter fieldWrapper);

}