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
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
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
public class TimbuctooActions {

  private final Authorizer authorizer;
  private final TransactionEnforcer transactionEnforcer;
  private final Clock clock;
  private final HandleAdder handleAdder;
  private final DataStoreOperations dataStoreOperations;
  private final AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  public TimbuctooActions(Authorizer authorizer, TransactionEnforcer transactionEnforcer, Clock clock,
                          HandleAdder handleAdder, DataStoreOperations dataStoreOperations,
                          AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
    this.authorizer = authorizer;
    this.transactionEnforcer = transactionEnforcer;
    this.clock = clock;
    this.handleAdder = handleAdder;
    this.dataStoreOperations = dataStoreOperations;
    this.afterSuccessTaskExecutor = afterSuccessTaskExecutor;
  }

  public UUID createEntity(Collection collection, Optional<Collection> baseCollection, CreateEntity createEntity,
                           String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    Change created = createChange(userId);
    createEntity.setCreated(created);

    dataStoreOperations.createEntity(collection, baseCollection, createEntity);

    afterSuccessTaskExecutor.addHandleTask(
      handleAdder,
      new HandleAdderParameters(collection.getCollectionName(), id, 1)
    );

    return id;
  }

  public void replaceEntity(Collection collection, UpdateEntity updateEntity, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException, AlreadyUpdatedException,
    IOException {
    checkIfAllowedToWrite(userId, collection);

    updateEntity.setModified(createChange(userId));

    int rev = dataStoreOperations.replaceEntity(collection, updateEntity);
    HandleAdderParameters params = new HandleAdderParameters(collection.getCollectionName(), updateEntity.getId(),
      rev);
    afterSuccessTaskExecutor.addHandleTask(handleAdder, params);

  }

  public void deleteEntity(Collection collection, UUID uuid, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    int rev = dataStoreOperations.deleteEntity(collection, uuid, createChange(userId));

    handleAdder.add(new HandleAdderParameters(collection.getCollectionName(), uuid, rev));
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
                              CustomEntityProperties customEntityProps,
                              CustomRelationProperties customRelationProps) throws NotFoundException {
    return dataStoreOperations.getEntity(id, rev, collection, customEntityProps, customRelationProps);
  }

  public DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                              boolean withRelations, CustomEntityProperties entityProps,
                                              CustomRelationProperties relationProps) {
    return dataStoreOperations.getCollection(collection, start, rows, withRelations, entityProps, relationProps);
  }


  public UUID createRelation(Collection collection, CreateRelation createRelation, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);

    // TODO make this method determine the id of the relation
    // createRelation.setId(id);
    createRelation.setCreated(createChange(userId));

    try {
      return dataStoreOperations.acceptRelation(collection, createRelation);
    } catch (RelationNotPossibleException e) {
      throw new IOException(e);
    }
  }


  public void replaceRelation(Collection collection, UpdateRelation updateRelation, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    updateRelation.setModified(createChange(userId));

    dataStoreOperations.replaceRelation(collection, updateRelation);
  }

  public static class TimbuctooActionsFactory {
    private final Authorizer authorizer;
    private final Clock clock;
    private final HandleAdder handleAdder;

    public TimbuctooActionsFactory(Authorizer authorizer, Clock clock, HandleAdder handleAdder) {
      this.authorizer = authorizer;
      this.clock = clock;
      this.handleAdder = handleAdder;
    }

    public TimbuctooActions create(TransactionEnforcer transactionEnforcer,
                                   DataStoreOperations dataStoreOperations,
                                   AfterSuccessTaskExecutor afterSuccessTaskExecutor
    ) {
      return new TimbuctooActions(
        authorizer,
        transactionEnforcer,
        clock,
        handleAdder,
        dataStoreOperations,
        afterSuccessTaskExecutor
      );
    }
  }
}

