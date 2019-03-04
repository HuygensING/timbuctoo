package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.core.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.DataStream;
import nl.knaw.huygens.timbuctoo.core.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.core.dto.QuickSearchResult;
import nl.knaw.huygens.timbuctoo.core.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.core.dto.RelationType;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.CreateProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.PredicateInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfReadProperty;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreMetadata;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DataStoreOperations extends AutoCloseable {

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

  List<QuickSearchResult> doQuickSearch(Collection collection, QuickSearch quickSearch, int limit);

  List<QuickSearchResult> doKeywordQuickSearch(Collection collection, String keywordType, QuickSearch quickSearch,
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

  void deleteVre(String vreName);

  byte[] getVreImageBlob(String vreName);

  void addPid(UUID id, int rev, URI pidUri) throws NotFoundException;

  /**
   * Get the latest version of the entity with a certain rdf uri.
   */
  Optional<ReadEntity> getEntityByRdfUri(Collection collection, String uri, boolean withRelations);

  List<RelationType> getRelationTypes();

  /**
   * Only adds the collection to the VRE when the VRE does not contain a collection with the same entity type name.
   */
  void addCollectionToVre(Vre vre, CreateCollection createCollection);

  void addPredicateValueTypeVertexToVre(Vre vre);

  void assertProperty(Vre vre, String entityRdfUri, RdfProperty property);

  void retractProperty(Vre vre, String entityRdfUri, RdfProperty property);

  /**
   * Tries to retrieve a property value.
   */
  Optional<RdfReadProperty> retrieveProperty(Vre vre, String entityRdfUri, String propertyUri);

  List<PredicateInUse> getPredicatesFor(Collection defaultCollection);

  /**
   * @return a list with the rdf uri's of the entities without type
   */
  List<String> getEntitiesWithUnknownType(Vre vre);

  /**
   * Adds the mandatory administrative fields (like id, created, modified, rev) to the entities imported by rdf.
   */
  void finishEntities(Vre vre, EntityFinisherHelper entityFinisherHelper);

  /**
   * @param collection the collection to add the properties to
   * @param createProperties the properties to add to the collection
   */
  void addPropertiesToCollection(Collection collection, List<CreateProperty> createProperties);
  
  void setAdminCollection(Collection collection, Collection adminCollection);
}
