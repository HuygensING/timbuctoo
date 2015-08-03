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
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Set;

public class RegularClientSearchResultMatcher extends CompositeMatcher<RegularSearchResultDTO> {

  private RegularClientSearchResultMatcher() {

  }

  public static RegularClientSearchResultMatcher likeRegularSearchResultDTO() {
    return new RegularClientSearchResultMatcher();
  }

  public RegularClientSearchResultMatcher withTerm(String term) {
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, String>("term", term) {
      @Override
      protected String getItemValue(RegularSearchResultDTO item) {
        return item.getTerm();
      }
    });

    return this;
  }

  public RegularClientSearchResultMatcher withFacets(List<Facet> facets) {
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, List<Facet>>("facets", facets) {
      @Override
      protected List<Facet> getItemValue(RegularSearchResultDTO item) {
        return item.getFacets();
      }
    });

    return this;
  }

  public RegularClientSearchResultMatcher withSortableFields(Set<String> sortableFields){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, Set<String>>("sortableFields", sortableFields) {
      @Override
      protected Set<String> getItemValue(RegularSearchResultDTO item) {
        return item.getSortableFields();
      }
    });
    return this;
  }

  public RegularClientSearchResultMatcher withFullTextSearchFields(Set<String> fullTextSearchFields){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, Set<String>>("fullTextSearchFields", fullTextSearchFields) {
      @Override
      protected Set<String> getItemValue(RegularSearchResultDTO item) {
        return item.getFullTextSearchFields();
      }
    });
    return this;
  }

  public RegularClientSearchResultMatcher withNumfound(int numfound) {
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, Integer>("numfound", numfound) {
      @Override
      protected Integer getItemValue(RegularSearchResultDTO item) {
        return item.getNumFound();
      }
    });
    return this;
  }

  public RegularClientSearchResultMatcher withIds(List<String> ids){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, List<String>>("ids", ids) {
      @Override
      protected List<String> getItemValue(RegularSearchResultDTO item) {
        return item.getIds();
      }
    });

    return this;
  }

  public RegularClientSearchResultMatcher withStart(int start){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, Integer>("start", start) {
      @Override
      protected Integer getItemValue(RegularSearchResultDTO item) {
        return item.getStart();
      }
    });
    return this;
  }

  public RegularClientSearchResultMatcher withRows(int rows){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, Integer>("rows", rows) {
      @Override
      protected Integer getItemValue(RegularSearchResultDTO item) {
        return item.getRows();
      }
    });
    return this;
  }



  public RegularClientSearchResultMatcher withNextLink(String nextLink){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, String>("nextLink", nextLink) {
      @Override
      protected String getItemValue(RegularSearchResultDTO item) {
        return item.getNextLink();
      }
    });

    return this;
  }

  public RegularClientSearchResultMatcher withPrevLink(String prevLink){
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, String>("prevLink", prevLink) {
      @Override
      protected String getItemValue(RegularSearchResultDTO item) {
        return item.getPrevLink();
      }
    });

    return this;
  }

  public Matcher<RegularSearchResultDTO> withRefs(List<DomainEntityDTO> refs) {
    this.addMatcher(new PropertyEqualtityMatcher<RegularSearchResultDTO, List<DomainEntityDTO>>("refs", refs) {
      @Override
      protected List<DomainEntityDTO> getItemValue(RegularSearchResultDTO item) {
        return item.getRefs();
      }
    });
    return this;
  }
}
