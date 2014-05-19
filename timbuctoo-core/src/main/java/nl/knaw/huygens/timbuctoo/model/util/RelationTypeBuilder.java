package nl.knaw.huygens.timbuctoo.model.util;

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

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationType;

public class RelationTypeBuilder {

  private String targetTypeName;
  private String sourceTypeName;

  private RelationTypeBuilder() {}

  public static RelationTypeBuilder newInstance() {
    return new RelationTypeBuilder();
  }

  public RelationTypeBuilder withSourceType(Class<? extends DomainEntity> type) {
    sourceTypeName = TypeNames.getInternalName(type);
    return this;
  }

  public RelationTypeBuilder withSourceTypeName(String typeName) {
    sourceTypeName = typeName;
    return this;
  }

  public RelationTypeBuilder withTargetType(Class<? extends DomainEntity> type) {
    targetTypeName = TypeNames.getInternalName(type);
    return this;
  }

  public RelationTypeBuilder withTargetTypeName(String typeName) {
    targetTypeName = typeName;
    return this;
  }

  public RelationType build() {
    RelationType relationType = new RelationType();

    relationType.setTargetTypeName(targetTypeName);
    relationType.setSourceTypeName(sourceTypeName);

    return relationType;
  }

}
