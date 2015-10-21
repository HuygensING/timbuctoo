package nl.knaw.huygens.timbuctoo.model;

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

import com.google.common.base.Joiner;
import nl.knaw.huygens.timbuctoo.config.Paths;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Map;

public class RelationDTO {

  private String type; // internal type
  private String id;
  private String path;
  private String relationName;
  private String sourceName;
  private String targetName;
  private Map<String, ? extends Object> sourceData;
  private Map<String, ? extends Object> targetData;

  public RelationDTO(String type, String xtype, String id, String relationName, DomainEntity source, DomainEntity target) {
    this.type = type;
    this.id = id;
    createPath(xtype, id);
    this.relationName = relationName;

    if (source != null) {
      sourceName = source.getIdentificationName();
      sourceData = source.getClientRepresentation();
    } else {
      sourceName = "[unknown]";
      sourceData = null;
    }
    if (target != null) {
      targetName = target.getIdentificationName();
      targetData = target.getClientRepresentation();
    } else {
      targetName = "[unknown]";
      targetData = null;
    }
  }

  public RelationDTO() {

  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public String getRelationName() {
    return relationName;
  }

  public String getSourceName() {
    return sourceName;
  }

  public String getTargetName() {
    return targetName;
  }

  public Map<String, ? extends Object> getSourceData() {
    return sourceData;
  }

  public Map<String, ? extends Object> getTargetData() {
    return targetData;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public void setSourceData(Map<String, ? extends Object> sourceData) {
    this.sourceData = sourceData;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public void setTargetData(Map<String, ? extends Object> targetData) {
    this.targetData = targetData;
  }

  public void setType(String type) {
    this.type = type;
  }


  public void setId(String id) {
    this.id = id;
  }

  public void createPath(String xtype, String id) {
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this);
  }
}
