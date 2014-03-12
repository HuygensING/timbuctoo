package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Index {

  /**
   * Adds the variations to an index.
   * @param variationsToAdd
   */
  public void add(List<? extends DomainEntity> variationsToAdd);
}
