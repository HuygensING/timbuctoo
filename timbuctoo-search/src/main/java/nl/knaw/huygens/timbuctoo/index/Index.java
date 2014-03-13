package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Index {

  /**
   * Adds new items to the index.
   * @param variations
   * @throws IndexException when the action fails.
   */
  public void add(List<? extends DomainEntity> variations) throws IndexException;

  /**
   * Updates existing items in the index.
   * @param variations
   * @throws IndexException when the action fails.
   */
  public void update(List<? extends DomainEntity> variations) throws IndexException;

}
