package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopQuery;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public interface TimbuctooQuery {

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  TinkerPopQuery hasNotNullProperty(String name, Object value);

  // TODO: make graph database independent 
  GraphQuery createGraphQuery(Graph db);

}