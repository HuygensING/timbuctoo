package nl.knaw.huygens.solr;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.List;

import com.google.common.collect.Lists;

public class RelationSearchParameters {

  private String typeString;
  private String sourceSearchId;
  private String targetSearchId;
  private List<String> relationTypeIds = Lists.newArrayList();

  public String getTypeString() {
    return typeString;
  }

  public void setTypeString(String typeString) {
    this.typeString = typeString;
  }

  public String getSourceSearchId() {
    return sourceSearchId;
  }

  public void setSourceSearchId(String sourceSearchId) {
    this.sourceSearchId = sourceSearchId;
  }

  public String getTargetSearchId() {
    return targetSearchId;
  }

  public void setTargetSearchId(String targetSearchId) {
    this.targetSearchId = targetSearchId;
  }

  public List<String> getRelationTypeIds() {
    return relationTypeIds;
  }

  public void setRelationTypeIds(List<String> relationTypeIds) {
    this.relationTypeIds = relationTypeIds;
  }

}
