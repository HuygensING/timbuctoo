package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Represents a Lucene index.
 */
public interface EntityIndex<T extends Entity> {

  void add(Class<T> entityType, String entityId) throws IndexException;

  void modify(Class<T> entityType, String entityId) throws IndexException;

  void remove(String entityId) throws IndexException;

  /**
   * Remove multiple entries from the index.
   * 
   * @param entityIds the id's of of the entries to remove
   * @throws IndexException encapsulates the exceptions generated while deleting.
   */
  void remove(List<String> entityIds) throws IndexException;

  void removeAll() throws IndexException;

  void flush() throws IndexException;

  QueryResponse search(Class<T> entityType, SolrQuery query) throws IndexException;

}
