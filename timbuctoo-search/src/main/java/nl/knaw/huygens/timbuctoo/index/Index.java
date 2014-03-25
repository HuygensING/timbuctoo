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

  /**
   * Delete an item from the index.
   * @param id the id of the item to delete.
   * @throws IndexException 
   */
  public void deleteById(String id) throws IndexException;

  /**
   * Delete multiple items by id.
   * @param ids the id's of the items to delete.  
   * @throws IndexException 
   */
  public void deleteById(List<String> ids) throws IndexException;

}
