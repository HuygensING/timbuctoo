package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.DeleteMessage.DeleteStatus.NOT_FOUND;

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

    TransactionState transactionState = dataAccess.createEntity(collection, baseCollection, createEntity);

    if (transactionState.wasCommitted()) {
      handleAdder.add(new HandleAdderParameters(collection.getCollectionName(), id, 1));
    }

    return id;
  }

  public void replaceEntity(Collection collection, UpdateEntity updateEntity, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException, AlreadyUpdatedException {
    checkIfAllowedToWrite(userId, collection);

    updateEntity.setModified(createChange(userId));

    UpdateReturnMessage updateReturnMessage = dataAccess.updateEntity(collection, updateEntity);

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
        throw new IllegalStateException("UpdateStatus '" + updateReturnMessage.getStatus() + "' is unknown.");
    }
  }

  public void deleteEntity(Collection collection, UUID uuid, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    DeleteMessage deleteMessage = dataAccess.deleteEntity(collection, uuid, createChange(userId));
    if (deleteMessage.getStatus() == NOT_FOUND) {
      throw new NotFoundException();
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

  public ReadEntity getEntity(Collection collection, UUID id, Integer rev,
                              CustomEntityProperties customEntityPros,
                              CustomRelationProperties customRelationProps) throws NotFoundException {
    GetMessage getMessage = dataAccess.getEntity(collection, id, rev, customEntityPros, customRelationProps);

    switch (getMessage.getStatus()) {
      case SUCCESS:
        return getMessage.getReadEntity().get();
      case NOT_FOUND:
        throw new NotFoundException();
      default:
        throw new IllegalStateException("GetStatus '" + getMessage.getStatus() + "' is unknown.");
    }
  }

  public DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                              boolean withRelations, CustomEntityProperties entityProps,
                                              CustomRelationProperties relationProps) {
    return dataAccess.getCollection(collection, start, rows, withRelations, entityProps, relationProps);
  }


  public UUID createRelation(Collection collection, CreateRelation createRelation, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);

    UUID id = UUID.randomUUID();
    createRelation.setId(id);
    createRelation.setCreated(createChange(userId));

    CreateMessage createMessage = dataAccess.createRelation(collection, createRelation);
    if (!createMessage.succeeded()) {
      throw new IOException(createMessage.getErrorMessage().get());
    }

    return id;
  }


  public void replaceRelation(Collection collection, UpdateRelation updateRelation, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    updateRelation.setModified(createChange(userId));

    UpdateReturnMessage updateMessage = dataAccess.updateRelation(collection, updateRelation);

    if (updateMessage.getStatus() == UpdateReturnMessage.UpdateStatus.NOT_FOUND) {
      throw new NotFoundException();
    }
  }
}

