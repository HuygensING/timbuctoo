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
}
