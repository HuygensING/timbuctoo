package nl.knaw.huygens.timbuctoo.index.solr;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import org.apache.solr.client.solrj.SolrQuery;

public class SolrQueryMatcher extends CompositeMatcher<SolrQuery> {

  private SolrQueryMatcher() {
  }

  public static SolrQueryMatcher likeSolrQuery() {
    return new SolrQueryMatcher();
  }

  public SolrQueryMatcher withQuery(String query) {
    this.addMatcher(new PropertyEqualtityMatcher<SolrQuery, String>("query", query) {
      @Override
      protected String getItemValue(SolrQuery item) {
        return item.getQuery();
      }
    });

    return this;
  }

  public SolrQueryMatcher withStart(int start) {
    this.addMatcher(new PropertyEqualtityMatcher<SolrQuery, Integer>("start", start) {
      @Override
      protected Integer getItemValue(SolrQuery item) {
        return item.getStart();
      }
    });
    return this;
  }

  public SolrQueryMatcher withRows(int rows) {
    this.addMatcher(new PropertyEqualtityMatcher<SolrQuery, Integer>("rows", rows) {
      @Override
      protected Integer getItemValue(SolrQuery item) {
        return item.getRows();
      }
    });
    return this;
  }
}
