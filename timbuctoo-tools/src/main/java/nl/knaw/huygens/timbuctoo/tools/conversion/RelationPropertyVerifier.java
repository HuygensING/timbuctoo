package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
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

import java.util.Map;

import com.google.common.base.Objects;

/**
 * A property verifier to check the equality of the source and target values of a Relation.
 */

public class RelationPropertyVerifier extends PropertyVerifier {
  private Map<String, String> oldIdNewIdMap;

  public RelationPropertyVerifier(Map<String, String> oldIdNewIdMap) {
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  @Override
  protected boolean areEqual(String fieldName, Object oldValue, Object newValue) {
    if (isSourceIdField(fieldName) || isTargetIdField(fieldName) || isTypeIdField(fieldName)) {

      String mappedValue = oldIdNewIdMap.get(oldValue);
      return Objects.equal(mappedValue, newValue);
    }

    return super.areEqual(fieldName, oldValue, newValue);
  }

  private boolean isTypeIdField(String fieldName) {
    return Objects.equal(fieldName, "typeId");
  }

  private boolean isTargetIdField(String fieldName) {
    return Objects.equal(fieldName, "targetId");
  }

  private boolean isSourceIdField(String fieldName) {
    return Objects.equal(fieldName, "sourceId");
  }
}
