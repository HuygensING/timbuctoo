package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public interface TimbuctooQuery {

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  TimbuctooQuery hasNotNullProperty(String name, Object value);

  /**
   * A method to search of a certain type
   * @param type the type to search for
   * @return the current instance
   */
  TimbuctooQuery hasType(Class<? extends Entity> type);

  // TODO: make graph database independent 
  GraphQuery createGraphQuery(Graph db);

}