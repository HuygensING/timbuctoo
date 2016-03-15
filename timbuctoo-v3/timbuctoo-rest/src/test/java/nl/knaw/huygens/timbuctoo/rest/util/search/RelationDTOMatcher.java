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
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;

import java.util.Map;

import static org.hamcrest.Matchers.endsWith;

public class RelationDTOMatcher extends CompositeMatcher<RelationDTO> {

  private RelationDTOMatcher() {
  }

  public static RelationDTOMatcher likeRelationDTO() {
    return new RelationDTOMatcher();
  }

  public RelationDTOMatcher withInternalType(String internalType) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, String>("internalType", internalType) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getType();
      }
    });
    return this;
  }

  public RelationDTOMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, String>("id", id) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getId();
      }
    });
    return this;
  }

  public RelationDTOMatcher withPathThatEndsWith(String path) {
    this.addMatcher(new PropertyMatcher<RelationDTO, String>("path", endsWith(path)) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getPath();
      }
    });
    return this;
  }

  public RelationDTOMatcher withRelationName(String relationName) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, String>("relationName", relationName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getRelationName();
      }
    });
    return this;
  }

  public RelationDTOMatcher withSourceName(String sourceName) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, String>("sourceName", sourceName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getSourceName();
      }
    });
    return this;
  }


  public RelationDTOMatcher withSourceData(Map<String, Object> sourceData) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, Map<String, ? extends Object>>("sourceData", sourceData) {
      @Override
      protected Map<String, ? extends Object> getItemValue(RelationDTO item) {
        return item.getSourceData();
      }
    });
    return this;
  }

  public RelationDTOMatcher withTargetName(String targetName) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, String>("targetName", targetName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getTargetName();
      }
    });
    return this;
  }


  public RelationDTOMatcher withTargetData(Map<String, Object> targetData) {
    this.addMatcher(new PropertyEqualityMatcher<RelationDTO, Map<String, ? extends Object>>("targetData", targetData) {
      @Override
      protected Map<String, ? extends Object> getItemValue(RelationDTO item) {
        return item.getTargetData();
      }
    });
    return this;
  }
}
