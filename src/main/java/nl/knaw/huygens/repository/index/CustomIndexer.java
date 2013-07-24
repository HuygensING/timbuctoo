package nl.knaw.huygens.repository.index;

import org.apache.solr.common.SolrInputDocument;

public interface CustomIndexer {
  public static class NoopIndexer implements CustomIndexer {
    public void indexItem(SolrInputDocument doc, Object item) {
      // No-op;
    }
    public String getFieldFilter() {
      return null;
    }
  }

  public void indexItem(SolrInputDocument doc, Object item);

  public String getFieldFilter();
}
