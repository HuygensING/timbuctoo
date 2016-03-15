package nl.knaw.huygens.timbuctoo.storage.graph;

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

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.projecta.SubARelation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;

public class SubARelationBuilder {
  private String sourceId;
  private String sourceType;
  private String targetId;
  private String tagetType;
  private String typeId;
  private String typeType;
  private String id;
  private boolean accepted;
  private int revision;
  private Change modified;
  private String pid;
  private List<String> variations;

  private SubARelationBuilder() {
  }

  public static SubARelationBuilder aRelation() {
    return new SubARelationBuilder();
  }

  public SubARelationBuilder withSourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public SubARelationBuilder withSourceType(String sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public SubARelationBuilder withTargetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  public SubARelationBuilder withTargetType(String tagetType) {
    this.tagetType = tagetType;
    return this;
  }

  public SubARelationBuilder withTypeId(String typeId) {
    this.typeId = typeId;
    return this;
  }

  public SubARelationBuilder withTypeType(String typeType) {
    this.typeType = typeType;
    return this;
  }

  public SubARelationBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public SubARelationBuilder isAccepted(boolean accepted) {
    this.accepted = accepted;
    return this;
  }

  public SubARelationBuilder withRevision(int revision) {
    this.revision = revision;
    return this;
  }

  public SubARelationBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }

  public SubARelationBuilder withVariations(Class<?>... types) {
    this.variations = Arrays.stream(types).map(type -> getInternalName(type)).collect(Collectors.toList());
    return this;
  }

  public SubARelation build() {
    SubARelation relation = new SubARelation();
    relation.setId(id);
    relation.setSourceId(sourceId);
    relation.setSourceType(sourceType);
    relation.setTargetId(targetId);
    relation.setTargetType(tagetType);
    relation.setTypeId(typeId);
    relation.setTypeType(typeType);
    relation.setAccepted(accepted);
    relation.setRev(revision);
    relation.setModified(modified);
    relation.setPid(pid);
    relation.setVariations(variations);

    return relation;
  }

  public SubARelationBuilder withAPID() {
    this.pid = "pid";
    return this;
  }


}
