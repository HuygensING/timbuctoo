package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

public class TinkerPopCreateRelation
  implements Function<DataAccessMethods, TransactionStateAndResult<CreateMessage>> {
  private static final Logger LOG = LoggerFactory.getLogger(TinkerPopCreateRelation.class);
  private final Collection collection;
  private final CreateRelation createRelation;

  public TinkerPopCreateRelation(Collection collection, CreateRelation createRelation) {
    this.collection = collection;
    this.createRelation = createRelation;
  }

  @Override
  public TransactionStateAndResult<CreateMessage> apply(DataAccessMethods dataAccessMethods) {
    try {
      UUID id = dataAccessMethods.acceptRelation(
        createRelation.getSourceId(),
        createRelation.getTypeId(),
        createRelation.getTargetId(),
        collection,
        createRelation.getCreated().getUserId(),
        Instant.ofEpochMilli(createRelation.getCreated().getTimeStamp())
      );
      return TransactionStateAndResult.commitAndReturn(CreateMessage.success(id));
    } catch (RelationNotPossibleException e) {
      LOG.error("Relation could not be created", e);
      return TransactionStateAndResult.rollbackAndReturn(CreateMessage.failure(e.getMessage()));
    }
  }
}
