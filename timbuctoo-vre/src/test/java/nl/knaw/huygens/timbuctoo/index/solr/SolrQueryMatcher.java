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

public class SolrQueryMatcher extends CompositeMatcher<SolrQuery> {

  private SolrQueryMatcher() {
  }

  public static SolrQueryMatcher likeSolrQuery() {
    return new SolrQueryMatcher();
  }

  public SolrQueryMatcher withQuery(String query) {
    this.addMatcher(new PropertyEqualityMatcher<SolrQuery, String>("query", query) {
      @Override
      protected String getItemValue(SolrQuery item) {
        return item.getQuery();
      }
    });

    return this;
  }

  public SolrQueryMatcher withStart(int start) {
    this.addMatcher(new PropertyEqualityMatcher<SolrQuery, Integer>("start", start) {
      @Override
      protected Integer getItemValue(SolrQuery item) {
        return item.getStart();
      }
    });
    return this;
  }

  public SolrQueryMatcher withRows(int rows) {
    this.addMatcher(new PropertyEqualityMatcher<SolrQuery, Integer>("rows", rows) {
      @Override
      protected Integer getItemValue(SolrQuery item) {
        return item.getRows();
      }
    });
    return this;
  }
}
