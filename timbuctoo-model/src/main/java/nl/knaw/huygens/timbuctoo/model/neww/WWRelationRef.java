package nl.knaw.huygens.timbuctoo.model.neww;

/*
 * #%L
 * Timbuctoo model
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

import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class WWRelationRef extends RelationRef {

  private Datable date;

  public WWRelationRef() {

  }

  public WWRelationRef(String type, String xtype, String id, String displayName, String relationId, boolean accepted, int rev, String relationName, Datable date) {
    super(type, xtype, id, displayName, relationId, accepted, rev, relationName);
    this.date = date;
  }

  public Datable getDate() {
    return date;
  }

  public void setDate(Datable date) {
    this.date = date;
  }

}
