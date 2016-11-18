package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is performs all the steps needed to save entities relations, etc.
 */
public class TimbuctooActions {

  private final Authorizer authorizer;
  private final Clock clock;
  private final PersistentUrlCreator persistentUrlCreator;
  private final DataStoreOperations dataStoreOperations;
  private final AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  public TimbuctooActions(Authorizer authorizer, Clock clock,
                          PersistentUrlCreator persistentUrlCreator, DataStoreOperations dataStoreOperations,
                          AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
    this.authorizer = authorizer;
    this.clock = clock;
    this.persistentUrlCreator = persistentUrlCreator;
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

    afterSuccessTaskExecutor.addTask(
      new AddPersistentUrlTask(
        persistentUrlCreator,
        new HandleAdderParameters(collection.getCollectionName(), id, 1)
      )
    );

    return id;
  }

  public void replaceEntity(Collection collection, UpdateEntity updateEntity, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException, AlreadyUpdatedException,
    IOException {
    checkIfAllowedToWrite(userId, collection);

    updateEntity.setModified(createChange(userId));

    int rev = dataStoreOperations.replaceEntity(collection, updateEntity);
    afterSuccessTaskExecutor.addTask(
      new AddPersistentUrlTask(
        persistentUrlCreator,
        new HandleAdderParameters(collection.getCollectionName(), updateEntity.getId(), rev)
      )
    );

  }

  public void deleteEntity(Collection collection, UUID uuid, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    int rev = dataStoreOperations.deleteEntity(collection, uuid, createChange(userId));


    afterSuccessTaskExecutor.addTask(
      new AddPersistentUrlTask(
        persistentUrlCreator,
        new HandleAdderParameters(collection.getCollectionName(), uuid, rev)
      )
    );
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

  public Vres loadVres() {
    return dataStoreOperations.loadVres();
  }

  public void addPid(UUID id, int rev, URI pidUri) throws NotFoundException {
    dataStoreOperations.addPid(id, rev, pidUri);
  }

  static class AddPersistentUrlTask implements AfterSuccessTaskExecutor.Task {
    private final PersistentUrlCreator persistentUrlCreator;
    private final HandleAdderParameters parameters;

    public AddPersistentUrlTask(PersistentUrlCreator persistentUrlCreator, HandleAdderParameters parameters) {
      this.persistentUrlCreator = persistentUrlCreator;
      this.parameters = parameters;
    }

    @Override
    public void execute() throws Exception {
      persistentUrlCreator.add(parameters);
    }

    @Override
    public String getDescription() {
      return String.format("Add handle to '%s'", parameters);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }
  }

  public static class TimbuctooActionsFactory {
    private final Authorizer authorizer;
    private final Clock clock;
    private final PersistentUrlCreator persistentUrlCreator;

    public TimbuctooActionsFactory(Authorizer authorizer, Clock clock, PersistentUrlCreator persistentUrlCreator) {
      this.authorizer = authorizer;
      this.clock = clock;
      this.persistentUrlCreator = persistentUrlCreator;
    }

    public TimbuctooActions create(DataStoreOperations dataStoreOperations,
                                   AfterSuccessTaskExecutor afterSuccessTaskExecutor
    ) {
      return new TimbuctooActions(
        authorizer,
        clock,
        persistentUrlCreator,
        dataStoreOperations,
        afterSuccessTaskExecutor
      );
    }
  }
}

