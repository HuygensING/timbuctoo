package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is performs all the steps needed to save entities relations, etc.
 */
public class TimbuctooDbAccess {

  private final Authorizer authorizer;
  private final DataAccess dataAccess;
  private final Clock clock;
  private final HandleAdder handleAdder;

  public TimbuctooDbAccess(Authorizer authorizer, DataAccess dataAccess, Clock clock, HandleAdder handleAdder) {
    this.authorizer = authorizer;
    this.dataAccess = dataAccess;
    this.clock = clock;
    this.handleAdder = handleAdder;
  }

  public UUID createEntity(Collection collection, Optional<Collection> baseCollection, CreateEntity createEntity,
                           String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    Change created = createChange(userId);
    createEntity.setCreated(created);

    DbCreateEntity dbCreateEntity =
      dataAccess.createEntity(collection, baseCollection, createEntity);
    TransactionState transactionState = dataAccess.executeAndReturn(dbCreateEntity);

    if (transactionState.wasCommitted()) {
      handleAdder.add(new HandleAdderParameters(collection.getCollectionName(), id, 1));
    }

    return id;
  }

  public void replaceEntity(Collection collection, UpdateEntity updateEntity, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException, AlreadyUpdatedException {
    checkIfAllowedToWrite(userId, collection);

    updateEntity.setModified(createChange(userId));

    DbUpdateEntity dbUpdateEntity = dataAccess.updateEntity(collection, updateEntity);
    UpdateReturnMessage updateReturnMessage = dataAccess.executeAndReturn(dbUpdateEntity);

    switch (updateReturnMessage.getStatus()) {
      case SUCCESS:
        Integer rev = updateReturnMessage.getNewRev().get();
        HandleAdderParameters params = new HandleAdderParameters(collection.getCollectionName(), updateEntity.getId(),
          rev);
        handleAdder.add(params);
        break;
      case NOT_FOUND:
        throw new NotFoundException();
      case ALREADY_UPDATED:
        throw new AlreadyUpdatedException();
      default:
        throw new IllegalStateException("Update status '" + updateReturnMessage.getStatus() + "' is unknown.");
    }
  }

  private Change createChange(String userId) {
    Change change = new Change();
    change.setUserId(userId);
    change.setTimeStamp(clock.instant().toEpochMilli());
    return change;
  }

  private void checkIfAllowedToWrite(String userId, Collection collection) throws
    AuthorizationException, AuthorizationUnavailableException {
    if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
      throw AuthorizationException.notAllowedToCreate(collection.getCollectionName());
    }
  }
}

