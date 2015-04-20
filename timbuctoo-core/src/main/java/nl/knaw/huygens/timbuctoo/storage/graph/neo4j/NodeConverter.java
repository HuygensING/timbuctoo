package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import org.neo4j.graphdb.Node;

public interface NodeConverter<T extends Entity> extends PropertyContainerConverter<Node, T> {

  <U extends T> U convertToSubType(Class<U> type, Node node) throws ConversionException;

}