package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Index {

  /**
   * Adds the variations to an index.
   * @param variationsToAdd
   * @throws IndexException 
   */
  public void add(List<? extends DomainEntity> variationsToAdd) throws IndexException;
}
