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
  public void add(Class<Relation> type, String id) throws IndexException {
    try {
      Relation relation = storageManager.getDocument(type, id);
      if (relation == null) {
        LOG.error("Failed to retrieve relation {}", id);
      } else {
        Reference relTypeRef = relation.getTypeRef();
        RelationType relationType = storageManager.getDocument(RelationType.class, relTypeRef.getId());
        if (relationType == null) {
          LOG.error("Failed to retrieve relation type {}", relTypeRef.getId());
        } else {
          doIndex(relation.getId(), relationType, relation.getSourceRef(), relation.getTargetRef());
          if (relationType.isSymmetric()) {
            doIndex(relation.getId() + "s", relationType, relation.getTargetRef(), relation.getSourceRef());
          }
        }
      }
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  private void doIndex(String id, RelationType relationType, Reference sourceRef, Reference targetRef) throws SolrServerException, IOException {
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
    // TODO also remove derived relations
    try {
      server.delete(CORE, id);
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
