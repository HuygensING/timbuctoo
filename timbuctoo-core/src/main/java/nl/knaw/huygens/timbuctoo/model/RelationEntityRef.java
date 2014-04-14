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

public class RelationEntityRef extends EntityRef {

  private String relationId;
  private boolean accepted;
  private int rev;

  public RelationEntityRef() {}

  public RelationEntityRef(String type, String xtype, String id, String displayName, String relationId, boolean accepted, int rev) {
    super(type, xtype, id, displayName);
    this.rev = rev;
    setRelationId(relationId);
    setAccepted(accepted);
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
}
