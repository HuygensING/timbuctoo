package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Function;

public class TinkerPopUpdateRelation
  implements Function<DataAccessMethods, TransactionStateAndResult<UpdateReturnMessage>> {
  private static final Logger LOG = LoggerFactory.getLogger(TinkerPopUpdateRelation.class);
  private final Collection collection;
  private final UpdateRelation updateRelation;

  public TinkerPopUpdateRelation(Collection collection, UpdateRelation updateRelation) {
    this.collection = collection;
    this.updateRelation = updateRelation;
  }

  @Override
  public TransactionStateAndResult<UpdateReturnMessage> apply(DataAccessMethods dataAccessMethods) {
    try {
      dataAccessMethods
        .replaceRelation(collection, updateRelation.getId(), updateRelation.getRev(), updateRelation.getAccepted(),
          updateRelation.getModified().getUserId(),
          Instant.ofEpochMilli(updateRelation.getModified().getTimeStamp()));

      return TransactionStateAndResult.commitAndReturn(UpdateReturnMessage.success(updateRelation.getRev() + 1));
    } catch (NotFoundException e) {
      return TransactionStateAndResult.rollbackAndReturn(UpdateReturnMessage.notFound());
    }
  }
}
