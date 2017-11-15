package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.DataStream;
import nl.knaw.huygens.timbuctoo.core.dto.EntityLookup;
import nl.knaw.huygens.timbuctoo.core.dto.ImmutableCreateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.core.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.core.dto.QuickSearchResult;
import nl.knaw.huygens.timbuctoo.core.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.core.dto.RelationType;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreMetadata;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is performs all the steps needed to save entities relations, etc.
 */
public class TimbuctooActions implements AutoCloseable {

  private final PermissionFetcher permissionFetcher;
  private final Clock clock;
  private final PersistentUrlCreator persistentUrlCreator;
  private final UrlGenerator uriToRedirectToFromPersistentUrls;
  private final DataStoreOperations dataStoreOperations;
  private final AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  public TimbuctooActions(PermissionFetcher permissionFetcher, Clock clock, PersistentUrlCreator persistentUrlCreator,
                          UrlGenerator uriToRedirectToFromPersistentUrls, DataStoreOperations dataStoreOperations,
                          AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
    this.permissionFetcher = permissionFetcher;
    this.clock = clock;
    this.persistentUrlCreator = persistentUrlCreator;
    this.uriToRedirectToFromPersistentUrls = uriToRedirectToFromPersistentUrls;
    this.dataStoreOperations = dataStoreOperations;
    this.afterSuccessTaskExecutor = afterSuccessTaskExecutor;
  }

  public UUID createEntity(Collection collection, Optional<Collection> baseCollection,
                           Iterable<TimProperty<?>> properties, String userId)
    throws PermissionFetchingException, IOException {
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
    throws PermissionFetchingException, NotFoundException, AlreadyUpdatedException,
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
    throws PermissionFetchingException, NotFoundException {
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
    PermissionFetchingException {
    if (!permissionFetcher.getOldPermissions(userId,collection.getVreName()).contains(Permission.WRITE)) {
      throw new PermissionFetchingException("Write permission not pressent.");
    }
  }

  public ReadEntity getEntity(Collection collection, UUID id, Integer rev) throws NotFoundException {
    return getEntity(collection, id, rev,
      (entity, entityVertex) -> {
      }, (traversalSource, vre, target, relationRef) -> {
      });
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

  public List<QuickSearchResult> doQuickSearch(Collection collection, QuickSearch quickSearch, String keywordType,
                                               int limit) {
    if (collection.getAbstractType().equals("keyword")) {
      return dataStoreOperations.doKeywordQuickSearch(collection, keywordType, quickSearch, limit);
    }
    return dataStoreOperations.doQuickSearch(collection, quickSearch, limit);
  }

  public UUID createRelation(Collection collection, CreateRelation createRelation, String userId)
    throws PermissionFetchingException, IOException {
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
    throws PermissionFetchingException, NotFoundException {
    checkIfAllowedToWrite(userId, collection);

    updateRelation.setModified(createChange(userId));

    dataStoreOperations.replaceRelation(collection, updateRelation);
  }


  public void addPid(URI pidUri, EntityLookup entityLookup) throws NotFoundException {
    dataStoreOperations.addPid(entityLookup.getTimId(), entityLookup.getRev(), pidUri); //no collection?
  }

  //================== Metadata ==================
  public Vres loadVres() {
    return dataStoreOperations.loadVres();
  }

  public Collection getCollectionMetadata(String collectionName) throws InvalidCollectionException {
    Vres vres = loadVres();
    Optional<Collection> collection = vres.getCollection(collectionName);

    return collection.orElseThrow(() -> new InvalidCollectionException(collectionName));
  }

  //================== Transaction methods ==================
  @Override
  public void close() {
    dataStoreOperations.close();
  }

  public void success() {
    dataStoreOperations.success();
  }

  public void rollback() {
    dataStoreOperations.rollback();
  }

  //================== RDF ==================
  public Optional<ReadEntity> getEntityByRdfUri(Collection collection, String uri, boolean withRelations) {
    return dataStoreOperations.getEntityByRdfUri(collection, uri, withRelations);
  }

  public Vre getVre(String vreName) {
    return loadVres().getVre(vreName);
  }

  public List<RelationType> getRelationTypes() {
    return dataStoreOperations.getRelationTypes();
  }

  public boolean hasMappingErrors(String vreName) {
    return dataStoreOperations.hasMappingErrors(vreName);
  }

  public Map<String, Map<String, String>> getMappingErrors(String vreName) {
    return dataStoreOperations.getMappingErrors(vreName);
  }

  public void ensureVreExists(String vreName) {
    dataStoreOperations.ensureVreExists(vreName);
  }

  public void saveRmlMappingState(String vreName, String rdfData) {
    dataStoreOperations.saveRmlMappingState(vreName, rdfData);
  }

  /**
   * This method removes all the data previously imported into this data set.
   * If you want to add data to a data set use {@link #rdfUpdateImportSession(String, Function)}.
   */
  public void rdfCleanImportSession(String vreName, Function<RdfImportSession, TransactionState> sessionConsumer) {
    RdfImportSession session = RdfImportSession.cleanImportSession(
      vreName,
      dataStoreOperations,
      new EntityFinisherHelper(uriToRedirectToFromPersistentUrls, Clock.systemDefaultZone(), "rdf-importer")
    );

    rdfImportSession(sessionConsumer, session);
  }

  /**
   * This method makes it possible to add data to an existing data set.
   * This method will not clean the data set when it contains any data.
   * Use {@link #rdfCleanImportSession(String, Function)} to clean the old data of a data set.
   */
  public void rdfUpdateImportSession(String vreName, Function<RdfImportSession, TransactionState> sessionConsumer) {
    RdfImportSession session = RdfImportSession.updateImportSession(
      vreName,
      dataStoreOperations,
      new EntityFinisherHelper(uriToRedirectToFromPersistentUrls, Clock.systemDefaultZone(), "rdf-importer")
    );
    rdfImportSession(sessionConsumer, session);
  }

  private void rdfImportSession(Function<RdfImportSession, TransactionState> sessionConsumer,
                                RdfImportSession session) {
    try {
      TransactionState result = sessionConsumer.apply(session);
      if (result.wasCommitted()) {
        session.commit();
      } else {
        session.rollback();
      }
    } catch (RuntimeException e) {
      session.rollback();
      throw e;
    } finally {
      session.close();
    }
  }

  public void setVrePublishState(String vreName, Vre.PublishState publishState) {
    dataStoreOperations.setVrePublishState(vreName, publishState);
  }

  public void setVreMetadata(String vreName, VreMetadata vreMetadataUpdate) {
    dataStoreOperations.setVreMetadata(vreName, vreMetadataUpdate);
  }

  public void setVreImage(String vreName, byte[] uploadedBytes, MediaType mediaType) {
    dataStoreOperations.setVreImage(vreName, uploadedBytes, mediaType);
  }

  public byte[] getVreImageBlob(String vreName) {
    return dataStoreOperations.getVreImageBlob(vreName);
  }

  public void deleteVre(String vreName, User user) throws PermissionFetchingException {
    boolean isAdmin = permissionFetcher.getOldPermissions(user.getPersistentId(),vreName).contains(Permission.ADMIN);
    if (isAdmin) {
      dataStoreOperations.deleteVre(vreName);
    } else {
      throw new PermissionFetchingException("Admin permission not found.");
    }
  }


  //================== Inner classes ==================
  @FunctionalInterface
  public interface TimbuctooActionsFactory {
    TimbuctooActions create(AfterSuccessTaskExecutor afterSuccessTaskExecutor);
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

  public static class TimbuctooActionsFactoryImpl implements TimbuctooActionsFactory {
    private final PermissionFetcher permissionFetcher;
    private final Clock clock;
    private final PersistentUrlCreator persistentUrlCreator;
    private final UrlGenerator uriToRedirectToFromPersistentUrls;
    private final Supplier<DataStoreOperations> dataStoreOperationsSupplier;

    public TimbuctooActionsFactoryImpl(PermissionFetcher permissionFetcher, Clock clock,
                                       PersistentUrlCreator persistentUrlCreator,
                                       UrlGenerator uriToRedirectToFromPersistentUrls,
                                       Supplier<DataStoreOperations> dataStoreOperationsSupplier) {
      this.permissionFetcher = permissionFetcher;
      this.clock = clock;
      this.persistentUrlCreator = persistentUrlCreator;
      this.uriToRedirectToFromPersistentUrls = uriToRedirectToFromPersistentUrls;
      this.dataStoreOperationsSupplier = dataStoreOperationsSupplier;
    }

    @Override
    public TimbuctooActions create(AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
      return new TimbuctooActions(
        permissionFetcher,
        clock,
        persistentUrlCreator,
        uriToRedirectToFromPersistentUrls,
        dataStoreOperationsSupplier.get(),
        afterSuccessTaskExecutor
      );
    }
  }
}

