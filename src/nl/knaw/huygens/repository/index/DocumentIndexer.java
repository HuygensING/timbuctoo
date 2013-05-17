package nl.knaw.huygens.repository.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.events.Events;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 * Used for talking to a specific index on the Solr Server that matches the
 * class used as a generic parameter. Takes care of converting POJO objects
 * (that extend {@link nl.knaw.huygens.repository.model.Document
 * <code>Document</code>}) to {@link org.apache.solr.common.SolrInputDocument
 * <code>SolrInputDocument</code>}s.
 * 
 * Note that whenever you update documents through this index, it is the
 * caller's responsibility to call
 * {@link nl.knaw.huygens.repository.index.DocumentIndexer#flush flush} to
 * update the index and notify the world that this has happened.
 * 
 * @author Gijs
 * 
 * @param <T>
 *          The generic parameter specifying what kind of POJO objects are used,
 *          and (implicitly) which index to index them in.
 */
public class DocumentIndexer<T extends Document> {

  private final LocalSolrServer solrServer;
  private final String core;
  private final ModelIterator modelIterator;
  private final Hub hub;

  /**
   * Create a document indexer for this entity
   * 
   * @param type
   *          POJO specifying what kind of objects will be created, and in which
   *          index they will be stored.
   * @param iterator
   *          a ModelIterator instance that will be used to generate
   *          {@link org.apache.solr.common.SolrInputDocument
   *          <code>SolrInputDocument</code>}s for the POJOs passed to this class.
   * @param server
   *          the SolrServer to use for indexing
   * @param hub
   *          the Hub to use for notifications.
   */
  public DocumentIndexer(Class<T> type, ModelIterator iterator, LocalSolrServer server, Hub hub) {
    this.solrServer = server;
    this.modelIterator = iterator;
    this.hub = hub;
    core = Utils.coreForType(type);
  }

  /**
   * Add a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} to the index.
   * 
   * @param entities
   *          the <code>Document</code> to add.
   * @throws RepositoryException
   *          if adding the document fails for some reason.
   */
  public <Q extends T> void add(List<Q> entities) throws RepositoryException {
    try {
      solrServer.add(core, getSolrInputDocument(entities));
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Update a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} already in the index. The existing document will be
   * found using the ID of the entity you pass.
   * 
   * @param entity
   *          the <code>Document</code> and it's subtypes to update.
   * @throws RepositoryException
   *          if adding the document fails for some reason.
   */
  public <Q extends T> void modify(List<Q> entity) throws RepositoryException {
    try {
      solrServer.update(core, getSolrInputDocument(entity));
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Remove a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} from the index.
   * 
   * @param entity
   *          the <code>Document</code> to remove.
   * @throws RepositoryException
   *          if removing the document fails for some reason.
   */
  public void remove(List<T> docs) throws RepositoryException {
    if (docs.isEmpty()) {
      return;
    }
    try {
      solrServer.delete(core, docs.get(0).getId());
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Remove all items from the index.
   *
   * @throws RepositoryException
   *          if removing fails for some reason.
   */
  public void removeAll() throws RepositoryException {
    try {
      solrServer.deleteAll(core);
    } catch (IndexException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Commit all changes to the SolrServer, and notify the world that the index
   * has been changed. Use responsibly.
   * 
   * @throws RepositoryException
   */
  public void flush() throws RepositoryException {
    try {
      solrServer.commit(core);
      hub.publish(new Events.IndexChangedEvent());
    } catch (Exception ex) {
      throw new RepositoryException(ex);
    }
  }

  /**
   * Generate a {@link org.apache.solr.common.SolrInputDocument
   * <code>SolrInputDocument</code>} given the POJO object passed.
   * 
   * @param entities
   *          the document and it's subtypes that you want a SolrInputDocument for.
   * @return the corresponding SolrInputDocument
   */
  protected <Q extends T> SolrInputDocument getSolrInputDocument(List<Q> entities) {
    SolrInputDocument inputDocument = null;
    SolrInputDocGenerator indexer = null;
    for (Q entity : entities) {
      if (inputDocument == null) {
        indexer = new SolrInputDocGenerator(entity);
      } else {
        indexer = new SolrInputDocGenerator(entity, inputDocument);
      }
      modelIterator.processClass(indexer, entity.getClass());
      inputDocument = indexer.getResult();
    }
    return inputDocument;
  }

  /**
   * Obtain a map (of ID -> Description) of all items in this index. Useful for
   * eg. autocomplete functionality. For the latter, may be useful to add the
   * ability to specify a (prefix) query for the description.
   * 
   * @return All document IDs and descriptions
   */
  public Map<String, String> getAll() {
    try {
      return solrServer.getSimpleMap(core);
    } catch (SolrServerException e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }

}
