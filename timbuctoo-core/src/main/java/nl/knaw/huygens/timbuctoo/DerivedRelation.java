package nl.knaw.huygens.timbuctoo;

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

import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;

public class DerivedRelation extends Relation {
  private DerivedRelationDescription description;

  private DerivedRelation(){
    this.setTypeId("derived");
    this.setId("derived");
  }

  public static DerivedRelation aDerivedRelation(){
    return new DerivedRelation();
  }

  public DerivedRelation withDescription(DerivedRelationDescription description){
    this.description = description;
    return this;
  }

  public DerivedRelation withSource(String type, String id){
    this.setSourceType(type);
    this.setSourceId(id);

    return this;
  }

  public DerivedRelation withTarget(String type, String id){
    this.setTargetType(type);
    this.setTargetId(id);

    return this;
  }

  public RelationType getRelationType() {
    RelationType relationType = new RelationType();
    relationType.setTargetTypeName(this.getTargetType());
    relationType.setSourceTypeName(this.getSourceType());
    relationType.setRegularName(description.getDerivedTypeName());
    relationType.setId(this.getTypeId());

    return relationType;
  }
}
