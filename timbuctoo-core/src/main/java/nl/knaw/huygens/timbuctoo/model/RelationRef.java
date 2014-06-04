package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
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

/**
 * A reference to a relation, to be used in other entities.
 * The reference is partially denormalized by including the display name.
 */
public class RelationRef {

  private String type;
  private String id;
  private String path;
  private String displayName;
  private String relationId;
  private boolean accepted;
  private int rev;

  // For deserialization...
  public RelationRef() {}

  public RelationRef(String type, String xtype, String id, String displayName, String relationId, boolean accepted, int rev) {
    this.type = type;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    this.displayName = displayName;
    this.rev = rev;
    setRelationId(relationId);
    setAccepted(accepted);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getRelationId() {
    return relationId;
  }

  public void setRelationId(String relationId) {
    this.relationId = relationId;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }

  public int getRev() {
    return rev;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof RelationRef) {
      RelationRef that = (RelationRef) object;
      return (this.type == null ? that.type == null : this.type.equals(that.type)) //
          && (this.id == null ? that.id == null : this.id.equals(that.id));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (type == null ? 0 : type.hashCode());
    result = 31 * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return String.format("{%s,%s}", type, id);
  }

}
