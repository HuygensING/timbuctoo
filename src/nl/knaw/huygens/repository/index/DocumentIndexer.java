package nl.knaw.huygens.repository.index;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import nl.knaw.huygens.repository.events.Events;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

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
  private final LocalSolrServer localSolrServer;
  private final String core;
  private final ModelIterator modelIterator;
  private final Hub hub;

  /**
   * Create a document indexer for this entity
   * 
   * @param entity
   *          POJO specifying what kind of objects will be created, and in which
   *          index they will be stored.
   * @param iterator
   *          a ModelIterator instance that will be used to generate
   *          {@link org.apache.solr.common.SolrInputDocument
   *          <code>SolrInputDocument</code>}s for the POJOs passed to this
   *          class.
   * @param server
   *          the SolrServer to use for indexing
   * @param hub
   *          the Hub to use for notifications.
   */
  public DocumentIndexer(Class<T> entity, ModelIterator iterator, LocalSolrServer server, Hub hub) {
    this.localSolrServer = server;
    this.modelIterator = iterator;
    // FIXME in the repository world, this is no longer sufficient:
    this.core = entity.getSimpleName().toLowerCase();
    this.hub = hub;
  }

  /**
   * Add a {@link nl.knaw.huygens.repository.model.Document
   * <code>Document</code>} to the index.
   * 
   * @param entities
   *          the <code>Document</code> to add.
   * @throws RepositoryException
   *           if adding the document fails for some reason.
   */
  public <Q extends T> void add(Q... entities) throws RepositoryException {
    try {
      localSolrServer.add(core, getSolrInputDocument(entities));
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
   *          the <code>Document</code> to update.
   * @throws RepositoryException
   *           if adding the document fails for some reason.
   */
  public <Q extends T> void modify(Q entity) throws RepositoryException {
    try {
      localSolrServer.update(core, getSolrInputDocument(entity));
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
   *           if removing the document fails for some reason.
   */
  public <Q extends T> void remove(Q entity) throws RepositoryException {
    try {
      localSolrServer.delete(core, getSolrInputDocument(entity));
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
      localSolrServer.commit(core);
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
   *          the document and it's subtypes that you want a SolrInputDocument
   *          for.
   * @return the corresponding SolrInputDocument
   */
  protected <Q extends T> SolrInputDocument getSolrInputDocument(Q... entities) {
    SolrInputDocument inputDocument = null;
    SolrInputDocGenerator indexer = null;
    for (Q entity : entities) {
      if (inputDocument == null) {
        indexer = new SolrInputDocGenerator(entity);
      } else {
        indexer = new SolrInputDocGenerator(entity, inputDocument);
      }
      modelIterator.processMethods(indexer, entity.getClass().getMethods());
      inputDocument = indexer.getResult();
    }
    return inputDocument;
  }

  /**
   * Determines the index field name from the method name (only used if the
   * annotation doesn't specify a fieldname).
   * 
   * @param m
   *          the Method object for which a Solr field name should be generated.
   * @return the field name
   */
  public static String getFieldName(Method m) {
    String name = m.getName();
    String type = m.getReturnType().getSimpleName();
    String rv = name.startsWith("get") ? name.substring(3) : name; // eliminate
                                                                   // 'get' part
    String[] parts = StringUtils.splitByCharacterTypeCamelCase(rv);
    type = type.replaceAll("\\[\\]", "");
    if (type.equals("boolean")) {
      type = "b";
    } else if (type.equals("int") || type.equals("long")) {
      type = "i";
    } else {
      type = "s";
    }
    return "facet_" + type + "_" + StringUtils.join(parts, "_").toLowerCase();
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
      return localSolrServer.getSimpleMap(core);
    } catch (SolrServerException e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }
}
