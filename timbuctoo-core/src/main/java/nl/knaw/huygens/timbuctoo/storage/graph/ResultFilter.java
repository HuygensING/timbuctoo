package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;

public interface ResultFilter {

  /**
   * Set the properties that must have distinct values. 
   * @param disitinctProperties the properties
   */
  void setDistinctProperties(Set<String> disitinctProperties);

  void setType(Class<? extends Entity> type);

}