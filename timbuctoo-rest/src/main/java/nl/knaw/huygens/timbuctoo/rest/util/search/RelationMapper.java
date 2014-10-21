package nl.knaw.huygens.timbuctoo.rest.util.search;

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

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class RelationMapper {

  private final Repository repository;
  private final TypeRegistry registry;

  @Inject
  public RelationMapper(Repository repository, TypeRegistry registry) {
    this.repository = repository;
    this.registry = registry;
  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> List<RelationDTO> createRefs(VRE vre, Class<T> type, List<T> result) {
    Preconditions.checkArgument(Relation.class.isAssignableFrom(type), "Type %s is not a Relation", type);
    return createRelationDTOs(vre, (Class<? extends Relation>) type, (List<Relation>) result);
  }

  private List<RelationDTO> createRelationDTOs(VRE vre, Class<? extends Relation> type, List<Relation> relations) {
    List<RelationDTO> list = Lists.newArrayList();
    if (!relations.isEmpty()) {
      String itype = TypeNames.getInternalName(type);
      String xtype = TypeNames.getExternalName(type);
      Class<? extends DomainEntity> sourceType = getMappedType(vre, relations.get(0).getSourceType());
      Class<? extends DomainEntity> targetType = getMappedType(vre, relations.get(0).getTargetType());

      for (Relation relation : relations) {
        RelationType relationType = repository.getRelationTypeById(relation.getTypeId(), true);
        String relationName = relationType.getRegularName();
        DomainEntity source = repository.getEntityWithRelations(sourceType, relation.getSourceId());
        repository.addDerivedProperties(vre, source);
        DomainEntity target = repository.getEntityWithRelations(targetType, relation.getTargetId());
        repository.addDerivedProperties(vre, target);
        list.add(new RelationDTO(itype, xtype, relation.getId(), relationName, source, target));
      }
    }
    return list;
  }

  private Class<? extends DomainEntity> getMappedType(VRE vre, String typeName) {
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeName);
    if (type == null) {
      throw new IllegalStateException("Not a domain entity: " + typeName);
    }
    Class<? extends DomainEntity> mappedType = vre.mapPrimitiveType(type);
    return (mappedType != null) ? mappedType : type;
  }

}
