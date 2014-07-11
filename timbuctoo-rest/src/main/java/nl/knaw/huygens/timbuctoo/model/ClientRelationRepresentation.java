package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.config.Paths;

import com.google.common.base.Joiner;

public class ClientRelationRepresentation {
  private final String type;
  private final String id;
  private final String path;
  private final String relationName;
  private final String sourceName;
  private final String targetName;

  public ClientRelationRepresentation(String type, String xtype, String id, String relationName, String sourceName, String targetName) {
    this.type = type;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    this.relationName = relationName;
    this.sourceName = sourceName;
    this.targetName = targetName;
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
}