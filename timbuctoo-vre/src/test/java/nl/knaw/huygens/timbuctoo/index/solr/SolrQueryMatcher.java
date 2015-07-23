package nl.knaw.huygens.timbuctoo.index.solr;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import org.apache.solr.client.solrj.SolrQuery;

public class SolrQueryMatcher extends CompositeMatcher<SolrQuery> {

  private SolrQueryMatcher() {
  }

  public static SolrQueryMatcher likeSolrQuery() {
    return new SolrQueryMatcher();
  }

  public SolrQueryMatcher withQuery(String query) {
    this.addMatcher(new PropertyMatcher<SolrQuery>("query", query) {
      @Override
      protected String getItemValue(SolrQuery item) {
        return item.getQuery();
      }
    });

    return this;
  }

  public SolrQueryMatcher withStart(int start) {
    // FIXME: change the code to an Integer matcher.
    this.addMatcher(new PropertyMatcher<SolrQuery>("start", "" + start) {
      @Override
      protected String getItemValue(SolrQuery item) {
        return "" + item.getStart();
      }
    });
    return this;
  }

  public SolrQueryMatcher withRows(int rows) {
    // FIXME: change the code to an Integer matcher.
    this.addMatcher(new PropertyMatcher<SolrQuery>("rows", "" + rows) {
      @Override
      protected String getItemValue(SolrQuery item) {
        return "" + item.getRows();
      }
    });
    return this;
  }
}
