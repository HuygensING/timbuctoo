package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Relation;

import com.tinkerpop.blueprints.Edge;

public interface EdgeConverter<T extends Relation> extends ElementConverter<T, Edge> {

  void removePropertyByFieldName(Edge edge, String fieldName);

}
