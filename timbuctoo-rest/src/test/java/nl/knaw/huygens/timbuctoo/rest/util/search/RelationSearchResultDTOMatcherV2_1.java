package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either NULL_VERSION 3 of the
 * License, or (at your option) any later NULL_VERSION.
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

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTOV2_1;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.nullValue;

class RelationSearchResultDTOV2_1Matcher extends CompositeMatcher<RelationSearchResultDTOV2_1> {

  private RelationSearchResultDTOV2_1Matcher() {

  }

  public static RelationSearchResultDTOV2_1Matcher likeRelationSearchResultDTOV2_1() {
    return new RelationSearchResultDTOV2_1Matcher();
  }

  public RelationSearchResultDTOV2_1Matcher withSortableFields(Set<String> sortableFields) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, Set<String>>("sortableFields", sortableFields) {
      @Override
      protected Set<String> getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getSortableFields();
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withNumFound(int numfound) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, Integer>("numfound", numfound) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getNumFound();
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withoutIds() {
    this.addMatcher(new PropertyMatcher<RelationSearchResultDTOV2_1, Object>("ids", nullValue()) {
      @Override
      protected List<String> getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getIds();
      }
    });

    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withStart(int start) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, Integer>("start", start) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getStart();
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withRows(int rows) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, Integer>("rows", rows) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getRows();
      }
    });
    return this;
  }


  public RelationSearchResultDTOV2_1Matcher withNextLink(String nextLink) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, String>("nextLink", nextLink) {
      @Override
      protected String getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getNextLink();
      }
    });

    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withPrevLink(String prevLink) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, String>("prevLink", prevLink) {
      @Override
      protected String getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getPrevLink();
      }
    });

    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withRefs(List<RelationDTO> refs) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, List<RelationDTO>>("refs", refs) {
      @Override
      protected List<RelationDTO> getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getRefs();
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withFullTextSearchField(String fullTextSearchField) {
    this.addMatcher(new PropertyMatcher<RelationSearchResultDTOV2_1, Iterable<? extends String>>("fullTextSearchFields", contains(fullTextSearchField)) {
      @Override
      protected Iterable<? extends String> getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getFullTextSearchFields();
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withFacet(Facet facet) {
    this.addMatcher(new PropertyMatcher<RelationSearchResultDTOV2_1, Facet[]>("facets", hasItemInArray(facet)) {
      @Override
      protected Facet[] getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getFacets().toArray(new Facet[]{});
      }
    });
    return this;
  }

  public RelationSearchResultDTOV2_1Matcher withTerm(String term) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTOV2_1, String>("term", term) {
      @Override
      protected String getItemValue(RelationSearchResultDTOV2_1 item) {
        return item.getTerm();
      }
    });

    return this;
  }
}
