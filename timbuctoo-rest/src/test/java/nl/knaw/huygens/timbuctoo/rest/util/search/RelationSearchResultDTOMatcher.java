package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;

import java.util.List;
import java.util.Set;

public class RelationSearchResultDTOMatcher extends CompositeMatcher<RelationSearchResultDTO> {

  private RelationSearchResultDTOMatcher() {

  }

  public static RelationSearchResultDTOMatcher likeRelationSearchResultDTO() {
    return new RelationSearchResultDTOMatcher();
  }

  public RelationSearchResultDTOMatcher withSortableFields(Set<String> sortableFields) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, Set<String>>("sortableFields", sortableFields) {
      @Override
      protected Set<String> getItemValue(RelationSearchResultDTO item) {
        return item.getSortableFields();
      }
    });
    return this;
  }

  public RelationSearchResultDTOMatcher withNumFound(int numfound) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, Integer>("numfound", numfound) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTO item) {
        return item.getNumFound();
      }
    });
    return this;
  }

  public RelationSearchResultDTOMatcher withIds(List<String> ids) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, List<String>>("ids", ids) {
      @Override
      protected List<String> getItemValue(RelationSearchResultDTO item) {
        return item.getIds();
      }
    });

    return this;
  }

  public RelationSearchResultDTOMatcher withStart(int start) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, Integer>("start", start) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTO item) {
        return item.getStart();
      }
    });
    return this;
  }

  public RelationSearchResultDTOMatcher withRows(int rows) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, Integer>("rows", rows) {
      @Override
      protected Integer getItemValue(RelationSearchResultDTO item) {
        return item.getRows();
      }
    });
    return this;
  }


  public RelationSearchResultDTOMatcher withNextLink(String nextLink) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, String>("nextLink", nextLink) {
      @Override
      protected String getItemValue(RelationSearchResultDTO item) {
        return item.getNextLink();
      }
    });

    return this;
  }

  public RelationSearchResultDTOMatcher withPrevLink(String prevLink) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, String>("prevLink", prevLink) {
      @Override
      protected String getItemValue(RelationSearchResultDTO item) {
        return item.getPrevLink();
      }
    });

    return this;
  }

  public RelationSearchResultDTOMatcher withRefs(List<RelationDTO> refs) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, List<RelationDTO>>("refs", refs) {
      @Override
      protected List<RelationDTO> getItemValue(RelationSearchResultDTO item) {
        return item.getRefs();
      }
    });
    return this;
  }

  public RelationSearchResultDTOMatcher withResults(List<? extends DomainEntity> results) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, List<? extends DomainEntity>>("results", results) {
      @Override
      protected List<? extends DomainEntity> getItemValue(RelationSearchResultDTO item) {
        return item.getResults();
      }
    });
    return this;
  }

  public RelationSearchResultDTOMatcher withSourceType(String sourceType) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, String>("sourceType", sourceType) {
      @Override
      protected String getItemValue(RelationSearchResultDTO item) {
        return item.getSourceType();
      }
    });

    return this;
  }

  public RelationSearchResultDTOMatcher withTargetType(String targetType) {
    this.addMatcher(new PropertyEqualityMatcher<RelationSearchResultDTO, String>("targetType", targetType) {
      @Override
      protected String getItemValue(RelationSearchResultDTO item) {
        return item.getTargetType();
      }
    });
    return this;
  }

}
