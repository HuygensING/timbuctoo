package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.DbUpdateEntity;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.UpdateReturnMessage;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.io.IOException;

public class TinkerPopUpdateEntity implements DbUpdateEntity {
  private final UpdateEntity updateEntity;
  private final Collection collection;

  public TinkerPopUpdateEntity(Collection collection, UpdateEntity updateEntity) {
    this.collection = collection;
    this.updateEntity = updateEntity;
  }

  @Override
  public TransactionStateAndResult<UpdateReturnMessage> apply(DataAccessMethods dataAccessMethods) {
    try {
      int newRev = dataAccessMethods.replaceEntity(collection, updateEntity);
      return TransactionStateAndResult.commitAndReturn(UpdateReturnMessage.success(newRev));
    } catch (NotFoundException e) {
      return TransactionStateAndResult.rollbackAndReturn(UpdateReturnMessage.notFound());
    } catch (AlreadyUpdatedException e) {
      return TransactionStateAndResult.rollbackAndReturn(UpdateReturnMessage.allreadyUpdated());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
