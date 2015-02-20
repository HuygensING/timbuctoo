package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.google.inject.Inject;

public class Neo4JStorage implements Storage {

  private final EntityWrapperFactory objectWrapperFactory;
  private final GraphDatabaseService db;

  @Inject
  public Neo4JStorage(GraphDatabaseService db, EntityWrapperFactory objectWrapperFactory) {
    this.db = db;
    this.objectWrapperFactory = objectWrapperFactory;
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Entity> String getStatistics(Class<T> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      try {
        EntityWrapper<T> objectWrapper = objectWrapperFactory.createFromInstance(type, entity);
        Node node = db.createNode();

        objectWrapper.addValuesToNode(node);
        objectWrapper.addAdministrativeValues(node);

        transaction.success();
        return objectWrapper.getId();
      } catch (IllegalArgumentException | IllegalAccessException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteVariation(Class<? extends DomainEntity> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public void declineRelationsOfEntity(Class<? extends Relation> type, String id) throws IllegalArgumentException, StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, ID_PROPERTY_NAME, id);

    ResourceIterator<Node> iterator = foundNodes.iterator();

    if (!iterator.hasNext()) {
      return null;
    }

    Node node = iterator.next();

    EntityWrapper<T> entityWrapper;
    try {
      entityWrapper = objectWrapperFactory.createFromType(type);
      return entityWrapper.createEntityFromNode(node);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new StorageException(e);
    }

  }

  @Override
  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return false;
  }

}
