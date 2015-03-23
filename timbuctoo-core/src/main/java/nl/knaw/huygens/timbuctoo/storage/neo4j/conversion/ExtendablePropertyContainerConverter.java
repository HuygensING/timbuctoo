package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

interface ExtendablePropertyContainerConverter<U extends PropertyContainer, T extends Entity> {

  void addPropertyConverter(PropertyConverter fieldWrapper);

}