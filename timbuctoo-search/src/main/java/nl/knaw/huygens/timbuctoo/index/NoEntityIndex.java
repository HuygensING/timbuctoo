package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * An {@code EntityIndex} that does nothing...
 */
class NoEntityIndex<T extends Entity> implements EntityIndex<T> {

  @Override
  public void add(Class<T> type, String id) {}

  @Override
  public void modify(Class<T> type, String id) {}

  @Override
  public void remove(String id) {}

  @Override
  public void remove(List<String> ids) {}

  @Override
  public void removeAll() {}

  @Override
  public void flush() {}

  @Override
  public QueryResponse search(Class<T> entityType, SolrQuery query) {
    return new QueryResponse();
  }

}
