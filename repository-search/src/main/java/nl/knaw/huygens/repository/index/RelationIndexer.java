package nl.knaw.huygens.repository.index;

import java.io.IOException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Reference;
import nl.knaw.huygens.repository.model.Relation;
import nl.knaw.huygens.repository.model.RelationType;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationIndexer implements DocumentIndexer<Relation> {

  private static final Logger LOG = LoggerFactory.getLogger(RelationIndexer.class);
  private static final String CORE = "relation";

  private final DocTypeRegistry registry;
  private final StorageManager storageManager;
  private final LocalSolrServer server;

  public RelationIndexer(DocTypeRegistry registry, StorageManager storageManager, LocalSolrServer server) {
    this.registry = registry;
    this.storageManager = storageManager;
    this.server = server;
  }

  @Override
  public void add(Class<Relation> docType, String docId) throws IndexException {
    Relation relation = storageManager.getDocument(docType, docId);
    if (relation == null) {
      LOG.error("Failed to retrieve relation {}", docId);
      throw new IndexException("Failed to index relation");
    }
    Reference typeRef = relation.getTypeRef();
    RelationType relType = storageManager.getDocument(RelationType.class, typeRef.getId());
    if (relType == null) {
      LOG.error("Failed to retrieve relation type {}", typeRef.getId());
      throw new IndexException("Failed to index relation");
    }
    try {
      doAdd(docId, relType, relation.getSourceRef(), relation.getTargetRef());
      if (relType.isSymmetric()) {
        doAdd(docId + "s", relType, relation.getTargetRef(), relation.getSourceRef());
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
      throw new IndexException("Failed to index relation");
    }
  }

  private void doAdd(String id, RelationType relationType, Reference sourceRef, Reference targetRef) throws SolrServerException, IOException {
    SolrInputDocument solrDoc = new SolrInputDocument();
    solrDoc.addField("id", id);
    solrDoc.addField("dynamic_s_type_id", relationType.getId());
    solrDoc.addField("dynamic_s_type_name", relationType.getRelTypeName());
    Document sourceDoc = getDocument(sourceRef);
    solrDoc.addField("dynamic_s_source_type", sourceRef.getType());
    solrDoc.addField("dynamic_s_source_id", sourceRef.getId());
    solrDoc.addField("dynamic_s_source_name", sourceDoc.getDisplayName());
    Document targetDoc = getDocument(targetRef);
    solrDoc.addField("dynamic_s_target_type", targetRef.getType());
    solrDoc.addField("dynamic_s_target_id", targetRef.getId());
    solrDoc.addField("dynamic_s_target_name", targetDoc.getDisplayName());
    server.add(CORE, solrDoc);
  }

  private Document getDocument(Reference reference) {
    Class<? extends Document> type = registry.getTypeForIName(reference.getType());
    return storageManager.getDocument(type, reference.getId());
  }

  @Override
  public void modify(Class<Relation> type, String id) throws IndexException {
    add(type, id);
  }

  @Override
  public void remove(String id) throws IndexException {
    try {
      String query = String.format("id:%s*", id);
      server.deleteByQuery(CORE, query);
    } catch (Exception e) {
      throw new IndexException(e);
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

}
