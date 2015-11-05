package nl.knaw.huygens.timbuctoo.storage;

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
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class RelationMatcher extends CompositeMatcher<Relation> {
  private RelationMatcher() {}

  public static RelationMatcher likeRelation() {
    return new RelationMatcher();
  }

  public RelationMatcher withSourceId(String sourceId) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>(Relation.SOURCE_ID, sourceId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getSourceId();
      }
    });

    return this;
  }

  public RelationMatcher withSourceType(String sourceType) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>("sourceType", sourceType) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getSourceType();
      }

    });

    return this;
  }

  public RelationMatcher withTargetId(String targetId) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>(Relation.TARGET_ID, targetId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTargetId();
      }
    });

    return this;
  }

  public RelationMatcher withTargetType(String targetType) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>("targetType", targetType) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTargetType();
      }

    });

    return this;
  }

  public RelationMatcher withTypeId(String typeId) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>(Relation.TYPE_ID, typeId) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTypeId();
      }
    });

    return this;
  }

  public RelationMatcher isAccepted(boolean accepted) {
    addMatcher(new PropertyEqualityMatcher<Relation, Boolean>("accepted", accepted) {

      @Override
      protected Boolean getItemValue(Relation item) {
        return item.isAccepted();
      }
    });

    return this;
  }

  public RelationMatcher withTypeType(String typeType) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>("typeType", typeType) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getTypeType();
      }
    });
    return this;
  }

  public RelationMatcher withRevision(int revision) {
    addMatcher(new PropertyEqualityMatcher<Relation, Integer>("rev", revision) {

      @Override
      protected Integer getItemValue(Relation item) {
        return item.getRev();
      }
    });
    return this;
  }

  public RelationMatcher withId(String id) {
    addMatcher(new PropertyEqualityMatcher<Relation, String>("id", id) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getId();
      }
    });
    return this;
  }

  public RelationMatcher withType(Class<? extends Relation> type) {
    addMatcher(new PropertyEqualityMatcher<Relation, Class<? extends Relation>>("type", type) {

      @Override
      protected Class<? extends Relation> getItemValue(Relation item) {
        return item.getClass();
      }

    });
    return this;
  }

  public RelationMatcher withPID() {
    addMatcher(new PropertyMatcher<Relation, String>("pid", is(not(nullValue(String.class)))) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getPid();
      }
    });

    return this;
  }

  public RelationMatcher withoutPID() {
    addMatcher(new PropertyMatcher<Relation, String>("pid", is(nullValue(String.class))) {

      @Override
      protected String getItemValue(Relation item) {
        return item.getPid();
      }
    });

    return this;
  }

  public RelationMatcher withModified(Change modified) {
    addMatcher(new PropertyEqualityMatcher<Relation, Change>("modified", modified) {

      @Override
      protected Change getItemValue(Relation item) {
        return item.getModified();
      }
    });

    return this;
  }
}
