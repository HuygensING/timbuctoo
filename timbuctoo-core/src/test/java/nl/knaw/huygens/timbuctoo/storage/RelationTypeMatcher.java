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
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationType;

public class RelationTypeMatcher extends CompositeMatcher<RelationType> {
  private RelationTypeMatcher() {
    super();
  }

  public static RelationTypeMatcher matchesRelationType() {
    return new RelationTypeMatcher();
  }

  public RelationTypeMatcher withId(String id) {
    addMatcher(new PropertyMatcher<RelationType, String>("id", id) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getId();
      }
    });

    return this;
  }

  public RelationTypeMatcher withRegularName(String regularName) {
    addMatcher(new PropertyMatcher<RelationType, String>("regularName", regularName) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getRegularName();
      }
    });

    return this;
  }

  public RelationTypeMatcher withInverseName(String inverseName) {
    addMatcher(new PropertyMatcher<RelationType, String>("inverseName", inverseName) {

      @Override
      protected String getItemValue(RelationType item) {
        return item.getInverseName();
      }
    });

    return this;
  }
}
