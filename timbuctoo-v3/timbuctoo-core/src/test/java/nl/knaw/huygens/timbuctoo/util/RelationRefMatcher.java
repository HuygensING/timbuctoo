package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
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
import nl.knaw.huygens.timbuctoo.model.RelationRef;

public class RelationRefMatcher extends CompositeMatcher<RelationRef> {
  protected RelationRefMatcher() {
  }

  public static RelationRefMatcher likeRelationRef() {
    return new RelationRefMatcher();
  }

  public RelationRefMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("id", id) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getId();
      }
    });

    return this;
  }

  public RelationRefMatcher withType(String type) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("type", type) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getType();
      }
    });
    return this;
  }

  public RelationRefMatcher withPath(String path) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("path", path) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getPath();
      }
    });

    return this;
  }

  public RelationRefMatcher withRelationName(String relationName) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("relationName", relationName) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getRelationName();
      }
    });
    return this;
  }

  public RelationRefMatcher withRev(int rev) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, Integer>("rev",  rev) {

      @Override
      protected Integer getItemValue(RelationRef item) {
        return  item.getRev();
      }
    });
    return this;
  }

  public RelationRefMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("displayName", displayName) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getDisplayName();
      }
    });
    return this;
  }

  public RelationRefMatcher withRelationId(String relationId) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, String>("relationId", relationId) {

      @Override
      protected String getItemValue(RelationRef item) {
        return item.getRelationId();
      }
    });
    return this;
  }

  public RelationRefMatcher withAccepted(boolean accepted) {
    this.addMatcher(new PropertyEqualityMatcher<RelationRef, Boolean>("accepted", accepted) {

      @Override
      protected Boolean getItemValue(RelationRef item) {
        return item.isAccepted();
      }
    });

    return this;
  }

}
