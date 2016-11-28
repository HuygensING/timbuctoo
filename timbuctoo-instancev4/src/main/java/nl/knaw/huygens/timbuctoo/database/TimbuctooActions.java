package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.EntityLookup;
import nl.knaw.huygens.timbuctoo.database.dto.ImmutableCreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.database.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is performs all the steps needed to save entities relations, etc.
 */
public class TimbuctooActions {

  private final Authorizer authorizer;
  private final Clock clock;
  private final PersistentUrlCreator persistentUrlCreator;
  private final UrlGenerator uriToRedirectToFromPersistentUrls;
  private final DataStoreOperations dataStoreOperations;
  private final AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  public TimbuctooActions(Authorizer authorizer, Clock clock, PersistentUrlCreator persistentUrlCreator,
                          UrlGenerator uriToRedirectToFromPersistentUrls, DataStoreOperations dataStoreOperations,
                          AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
    this.authorizer = authorizer;
    this.clock = clock;
    this.persistentUrlCreator = persistentUrlCreator;
    this.uriToRedirectToFromPersistentUrls = uriToRedirectToFromPersistentUrls;
    this.dataStoreOperations = dataStoreOperations;
    this.afterSuccessTaskExecutor = afterSuccessTaskExecutor;
  }

  public UUID createEntity(Collection collection, Optional<Collection> baseCollection,
                           Iterable<TimProperty<?>> properties, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);
    UUID id = UUID.randomUUID();
    Change created = createChange(userId);
    CreateEntity createEntity = ImmutableCreateEntity.builder()
      .properties(properties)
      .id(id)
      .created(created)
      .build();

    dataStoreOperations.createEntity(collection, baseCollection, createEntity);

    afterSuccessTaskExecutor.addTask(
      new AddPersistentUrlTask(
        persistentUrlCreator,
        uriToRedirectToFromPersistentUrls.apply(collection.getCollectionName(), id, 1),
        ImmutableEntityLookup.builder()
                             .rev(1)
                             .timId(id)
                             .collection(collection.getCollectionName())
                             .build()
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
        uriToRedirectToFromPersistentUrls.apply(collection.getCollectionName(), updateEntity.getId(), rev),
        ImmutableEntityLookup.builder()
                             .rev(rev)
                             .timId(updateEntity.getId())
                             .collection(collection.getCollectionName())
                             .build()
      )
    );
  }
  //FIXME: when adding the new datamodel. We need to fix the persistent url generator. It now generates a url per
  // collection, but writes to a property that exists regardless of the collection. It also generates a new persistent
  // url after you have deleted an entity (which therefore always 404's)

  public void deleteEntity(Collection collection, UUID uuid, String userId)
    throws AuthorizationUnavailableException, AuthorizationException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    int rev = dataStoreOperations.deleteEntity(collection, uuid, createChange(userId));


    afterSuccessTaskExecutor.addTask(
      new AddPersistentUrlTask(
        persistentUrlCreator,
        uriToRedirectToFromPersistentUrls.apply(collection.getCollectionName(), uuid, rev),
        ImmutableEntityLookup.builder()
                             .rev(rev)
                             .timId(uuid)
                             .collection(collection.getCollectionName())
                             .build()
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

  public List<ReadEntity> doQuickSearch(Collection collection, QuickSearch quickSearch, int limit) {
    return dataStoreOperations.doQuickSearch(collection, quickSearch, limit);
  }

  public List<ReadEntity> doKeywordQuickSearch(Collection collection, String keywordType, QuickSearch quickSearch,
                                               int limit) {
    return dataStoreOperations.doKeywordQuickSearch(collection, keywordType, quickSearch, limit);
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


  public void addPid(URI pidUri, EntityLookup entityLookup) throws NotFoundException {
    dataStoreOperations.addPid(entityLookup.getTimId(), entityLookup.getRev(), pidUri); //no collection?
  }

  //================== Metdata ==================
  public Vres loadVres() {
    return dataStoreOperations.loadVres();
  }

  public Collection getCollectionMetadata(String collectionName) throws InvalidCollectionException {
    Vres vres = loadVres();
    Optional<Collection> collection = vres.getCollection(collectionName);

    return collection.orElseThrow(() -> new InvalidCollectionException(collectionName));
  }


  static class AddPersistentUrlTask implements AfterSuccessTaskExecutor.Task {
    private final PersistentUrlCreator persistentUrlCreator;
    private final URI uriToRedirectTo;
    private final EntityLookup entityLookup;

    public AddPersistentUrlTask(PersistentUrlCreator persistentUrlCreator, URI uriToRedirectTo,
                                EntityLookup entityLookup) {
      this.persistentUrlCreator = persistentUrlCreator;
      this.uriToRedirectTo = uriToRedirectTo;
      this.entityLookup = entityLookup;
    }

    @Override
    public void execute() throws Exception {
      persistentUrlCreator.add(uriToRedirectTo, entityLookup);
    }

    @Override
    public String getDescription() {
      return String.format("Add handle to '%s'", entityLookup);
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
    private final UrlGenerator uriToRedirectToFromPersistentUrls;

    public TimbuctooActionsFactory(Authorizer authorizer, Clock clock, PersistentUrlCreator persistentUrlCreator,
                                   UrlGenerator uriToRedirectToFromPersistentUrls) {
      this.authorizer = authorizer;
      this.clock = clock;
      this.persistentUrlCreator = persistentUrlCreator;
      this.uriToRedirectToFromPersistentUrls = uriToRedirectToFromPersistentUrls;
    }

    public TimbuctooActions create(DataStoreOperations dataStoreOperations,
                                   AfterSuccessTaskExecutor afterSuccessTaskExecutor
    ) {
      return new TimbuctooActions(
        authorizer,
        clock,
        persistentUrlCreator,
        uriToRedirectToFromPersistentUrls,
        dataStoreOperations,
        afterSuccessTaskExecutor
      );
    }
  }
}

