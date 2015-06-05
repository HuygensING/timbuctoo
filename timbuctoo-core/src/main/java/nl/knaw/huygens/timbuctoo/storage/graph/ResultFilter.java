package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Set;

public interface ResultFilter {

  /**
   * Set the properties that must have distinct values. 
   * @param disitinctProperties the properties
   */
  void setDistinctProperties(Set<String> disitinctProperties);

}