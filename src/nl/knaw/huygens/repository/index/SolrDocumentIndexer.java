package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

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
 * {@link nl.knaw.huygens.repository.index.SolrDocumentIndexer#flush flush} to
 * update the index and notify the world that this has happened.
 * 
 * @author Gijs
 * 
 * @param <T>
 *          The generic parameter specifying what kind of POJO objects are used,
 *          and (implicitly) which index to index them in.
 */
class SolrDocumentIndexer<T extends Document> implements DocumentIndexer<T> {

  /**
   * Creates a new <code>SolrDocumentIndexer</code> instance.
   */
  public static <U extends Document> SolrDocumentIndexer<U> newInstance(Class<U> type, LocalSolrServer server) {
    return new SolrDocumentIndexer<U>(type, server);
  }

  private final LocalSolrServer solrServer;
  private final String core;
  private final ModelIterator modelIterator;

  /**
   * Creates a document indexer for the specified type.
   * 
   * @param type
   *          document type token
   * @param server
   *          the SolrServer to use for indexing
   */
  private SolrDocumentIndexer(Class<T> type, LocalSolrServer server) {
    this.solrServer = server;
    this.modelIterator = new ModelIterator();
    core = coreForType(type);
  }

  /**
   * Returns the Solr core for the specified document type.
   */
  private String coreForType(Class<? extends Document> type) {
    return type.getSimpleName().toLowerCase();
  }

  /**
   * Add a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} to the index.
   * 
   * @param entities
   *          the <code>Document</code> to add.
   * @throws IndexException
   *          if adding the document fails for some reason.
   */
  @Override
  public <U extends T> void add(List<U> entities) throws IndexException {
    try {
      solrServer.add(core, getSolrInputDocument(entities));
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Update a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} already in the index. The existing document will be
   * found using the ID of the entity you pass.
   * 
   * @param entity
   *          the <code>Document</code> and it's subtypes to update.
   * @throws IndexException
   *          if adding the document fails for some reason.
   */
  @Override
  public <U extends T> void modify(List<U> entity) throws IndexException {
    try {
      solrServer.add(core, getSolrInputDocument(entity));
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Remove a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} from the index.
   * 
   * @param id
   *          the id of the <code>Document</code> to remove.
   * @throws IndexException
   *          if removing the document fails for some reason.
   */
  @Override
  public void remove(String id) throws IndexException {
    try {
      solrServer.delete(core, id);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Remove all items from the index.
   *
   * @throws IndexException
   *          if removing fails for some reason.
   */
  @Override
  public void removeAll() throws IndexException {
    try {
      solrServer.deleteAll(core);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Commit all changes to the SolrServer, and notify the world that the index
   * has been changed. Use responsibly.
   * 
   * @throws IndexException
   */
  @Override
  public void flush() throws IndexException {
    try {
      solrServer.commit(core);
      // hub.publish(new Events.IndexChangedEvent());
    } catch (Exception ex) {
      throw new IndexException(ex);
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
  private <U extends T> SolrInputDocument getSolrInputDocument(List<U> entities) {
    SolrInputDocument inputDocument = null;
    SolrInputDocGenerator indexer = null;
    for (U entity : entities) {
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

}
