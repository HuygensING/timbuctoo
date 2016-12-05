package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationType;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataStoreOperations extends AutoCloseable {
  void clearMappingErrors(String vreName);

  boolean hasMappingErrors(String vreName);

  void success();

  void rollback();

  @Override
  void close();

  UUID acceptRelation(Collection collection, CreateRelation createRelation) throws RelationNotPossibleException;

  void createEntity(Collection col, Optional<Collection> baseCollection, CreateEntity input)
    throws IOException;

  ReadEntity getEntity(UUID id, Integer rev, Collection collection,
                       CustomEntityProperties customEntityProperties,
                       CustomRelationProperties customRelationProperties) throws NotFoundException;

  DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                       boolean withRelations,
                                       CustomEntityProperties customEntityProperties,
                                       CustomRelationProperties customRelationProperties);

  List<ReadEntity> doQuickSearch(Collection collection, QuickSearch quickSearch, int limit);

  List<ReadEntity> doKeywordQuickSearch(Collection collection, String keywordType, QuickSearch quickSearch,
                                        int limit);

  /**
   * Sets the new values of the entity contained in replaceEntity and removes the other values.
   *
   * @return the new revision of entity
   * @throws NotFoundException       when the entity does not exist in the database
   * @throws AlreadyUpdatedException when the entity is updated in between the read and this update
   */
  int replaceEntity(Collection collection, UpdateEntity updateEntity)
    throws NotFoundException, AlreadyUpdatedException, IOException;

  void replaceRelation(Collection collection, UpdateRelation updateRelation) throws NotFoundException;

  int deleteEntity(Collection collection, UUID id, Change modified)
      throws NotFoundException;

  Vres loadVres();

  boolean databaseIsEmptyExceptForMigrations();

  void initDb(Vres mappings, RelationType... relationTypes);

  void saveRelationTypes(RelationType... relationTypes);

  void saveVre(Vre vre);

  Vre ensureVreExists(String vreName);

  void removeCollectionsAndEntities(String vreName);

  void addPid(UUID id, int rev, URI pidUri) throws NotFoundException;
}
