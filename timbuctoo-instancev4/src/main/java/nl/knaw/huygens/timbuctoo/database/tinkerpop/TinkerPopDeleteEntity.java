package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.DeleteMessage;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.UUID;
import java.util.function.Function;

public class TinkerPopDeleteEntity implements
  Function<DataStoreOperations, TransactionStateAndResult<DeleteMessage>> {
  private final Collection collection;
  private final UUID id;
  private final Change modified;

  public TinkerPopDeleteEntity(Collection collection, UUID id, Change modified) {
    this.collection = collection;
    this.id = id;
    this.modified = modified;
  }

  @Override
  public TransactionStateAndResult<DeleteMessage> apply(DataStoreOperations dataStoreOperations) {
    try {
      dataStoreOperations.deleteEntity(collection, id, modified);
      return TransactionStateAndResult.commitAndReturn(DeleteMessage.success());
    } catch (NotFoundException e) {
      return TransactionStateAndResult.rollbackAndReturn(DeleteMessage.notFound());
    }
  }
}
