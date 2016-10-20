package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.TransactionState;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class TinkerPopCreateEntity
  implements Function<DataStoreOperations, TransactionStateAndResult<TransactionState>> {
  public static final Logger LOG = LoggerFactory.getLogger(TinkerPopCreateEntity.class);
  private final Collection col;
  private final Optional<Collection> baseCollection;
  private final CreateEntity input;
  private final String userId;
  private final Instant creationTime;
  private final UUID id;

  public TinkerPopCreateEntity(Collection col, Optional<Collection> baseCollection, CreateEntity input) {

    this.col = col;
    this.baseCollection = baseCollection;
    this.input = input;
    this.userId = input.getCreated().getUserId();
    this.creationTime = Instant.ofEpochMilli(input.getCreated().getTimeStamp());
    this.id = input.getId();
  }

  @Override
  public TransactionStateAndResult<TransactionState> apply(DataStoreOperations dataStoreOperations) {
    try {
      dataStoreOperations.createEntity(col, baseCollection, input);
      return TransactionStateAndResult.commitAndReturn(TransactionState.commit());
    } catch (IOException e) {
      LOG.error("Failing to create a new entity", e);
      return TransactionStateAndResult.rollbackAndReturn(TransactionState.rollback());
    }
  }
}
