package nl.knaw.huygens.timbuctoo.index.solr;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import org.apache.solr.client.solrj.SolrQuery;

public class SortClauseMatcher extends CompositeMatcher<SolrQuery.SortClause> {
  private SortClauseMatcher() {

  }

  public static SortClauseMatcher likeSortClause() {
    return new SortClauseMatcher();
  }

  public SortClauseMatcher withItem(String item) {
    this.addMatcher(new PropertyEqualityMatcher<SolrQuery.SortClause, String>("item", item) {
      @Override
      protected String getItemValue(SolrQuery.SortClause item) {
        return item.getItem();
      }
    });
    return this;
  }

  public SortClauseMatcher withOrder(SolrQuery.ORDER order) {
    this.addMatcher(new PropertyEqualityMatcher<SolrQuery.SortClause, SolrQuery.ORDER>("order", order) {
      @Override
      protected SolrQuery.ORDER getItemValue(SolrQuery.SortClause item) {
        return item.getOrder();
      }
    });
    return this;
  }

}
