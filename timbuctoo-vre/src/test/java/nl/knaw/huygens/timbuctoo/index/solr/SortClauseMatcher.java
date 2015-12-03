package nl.knaw.huygens.timbuctoo.index.solr;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
