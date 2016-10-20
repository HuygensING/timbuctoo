package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.UpdateReturnMessage;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.io.IOException;
import java.util.function.Function;

public class TinkerPopUpdateEntity implements
  Function<DataStoreOperations, TransactionStateAndResult<UpdateReturnMessage>> {
  private final UpdateEntity updateEntity;
  private final Collection collection;

  public TinkerPopUpdateEntity(Collection collection, UpdateEntity updateEntity) {
    this.collection = collection;
    this.updateEntity = updateEntity;
  }

  @Override
  public TransactionStateAndResult<UpdateReturnMessage> apply(DataStoreOperations dataStoreOperations) {
    try {
      int newRev = dataStoreOperations.replaceEntity(collection, updateEntity);
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
