package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.DbCreateEntity;
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

public class TinkerPopCreateEntity implements DbCreateEntity {
  public static final Logger LOG = LoggerFactory.getLogger(TinkerPopCreateEntity.class);
  private final Collection col;
  private final Optional<Collection> baseCollection;
  private final CreateEntity input;
  private final String userId;
  private final Instant creationTime;
  private final UUID id;

  public TinkerPopCreateEntity(Collection col, Optional<Collection> baseCollection, CreateEntity input, String userId,
                               Instant creationTime, UUID id) {

    this.col = col;
    this.baseCollection = baseCollection;
    this.input = input;
    this.userId = userId;
    this.creationTime = creationTime;
    this.id = id;
  }

  @Override
  public TransactionStateAndResult<TransactionState> apply(DataAccessMethods dataAccessMethods) {
    try {
      dataAccessMethods.createEntity(col, baseCollection, input, userId, creationTime, id);
      return TransactionStateAndResult.commitAndReturn(TransactionState.commit());
    } catch (IOException e) {
      LOG.error("Failing to create a new entity", e);
      return TransactionStateAndResult.rollbackAndReturn(TransactionState.rollback());
    }
  }
}
