package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public interface NodeConverter<T extends Entity> extends PropertyContainerConverter<Node, T> {

  String getPropertyName(String fieldName);

}