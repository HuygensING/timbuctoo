package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the Solr core for {@code Relation} entities.
 * 
 * Note that a single relation can give rise to multiple Solr documents.
 * The id's of these Solr documents have the id of the relation entity
 * as prefix, allowing them to be deleted by a simple query.
 */
class RelationIndex implements EntityIndex<Relation> {

  private static final Logger LOG = LoggerFactory.getLogger(RelationIndex.class);
  private static final String CORE = "relation";

  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private final StorageManager storageManager;
  private final RelationManager relationManager;

  public RelationIndex(TypeRegistry registry, LocalSolrServer server, StorageManager storageManager, RelationManager relationManager) {
    this.registry = registry;
    this.server = server;
    this.storageManager = storageManager;
    this.relationManager = relationManager;
  }

  @Override
  public void add(Class<Relation> docType, String docId) throws IndexException {
    Relation relation = storageManager.getEntity(docType, docId);
    if (relation == null) {
      LOG.error("Failed to retrieve relation {}", docId);
      throw new IndexException("Failed to index relation");
    }
    Reference typeRef = relation.getTypeRef();
    RelationType relType = relationManager.getRelationType(typeRef);
    if (relType == null) {
      LOG.error("Failed to retrieve relation type {}", typeRef.getId());
      throw new IndexException("Failed to index relation");
    }
    try {
      Reference sourceRef = relation.getSourceRef();
      Reference targetRef = relation.getTargetRef();
      doAdd(docId + "-r", relType.getId(), relType.getRegularName(), sourceRef, targetRef);
      doAdd(docId + "-i", relType.getId(), relType.getInverseName(), targetRef, sourceRef);
    } catch (Exception e) {
      LOG.error(e.getMessage());
      throw new IndexException("Failed to index relation");
    }
  }

  private void doAdd(String docId, String relTypeId, String relTypeName, Reference sourceRef, Reference targetRef) throws SolrServerException, IOException {
    SolrInputDocument solrDoc = new SolrInputDocument();
    solrDoc.addField("id", docId);
    // relation type
    solrDoc.addField("dynamic_k_type_id", relTypeId);
    solrDoc.addField("dynamic_k_type_name", relTypeName);
    // source entity
    solrDoc.addField("dynamic_k_source_type", sourceRef.getType());
    solrDoc.addField("dynamic_k_source_id", sourceRef.getId());
    // target entity
    Entity targetDoc = getEntity(targetRef);
    solrDoc.addField("dynamic_k_target_type", targetRef.getType());
    solrDoc.addField("dynamic_k_target_id", targetRef.getId());
    solrDoc.addField("dynamic_k_target_name", targetDoc.getDisplayName());
    server.add(CORE, solrDoc);
  }

  private Entity getEntity(Reference reference) {
    Class<? extends Entity> type = registry.getTypeForIName(reference.getType());
    return storageManager.getEntity(type, reference.getId());
  }

  @Override
  public void modify(Class<Relation> type, String id) throws IndexException {
    add(type, id);
  }

  @Override
  public void remove(String sourceId) throws IndexException {
    /*
     * deleteById cannot be used, because the id's of the relations are composed from the id's 
     * of the source and target entity. In the query the source id is used 
     * to delete all the relations of a certain entity.
     */
    try {
      String query = String.format("id:%s*", sourceId);
      server.deleteByQuery(CORE, query);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void remove(List<String> ids) throws IndexException {
    //TODO find a good way to remove multiple ids.
    //    try {
    //      server.deleteByQuery(CORE, );
    //    } catch (Exception e) {
    //      throw new IndexException(e);
    //    }

    for (String id : ids) {
      remove(id);
    }
  }

  @Override
  public void removeAll() throws IndexException {
    try {
      server.deleteAll(CORE);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void flush() throws IndexException {
    try {
      server.commit(CORE);
    } catch (Exception ex) {
      throw new IndexException(ex);
    }
  }

  @Override
  public QueryResponse search(Class<Relation> entityType, SolrQuery query) {
    return new QueryResponse();
  }

}
