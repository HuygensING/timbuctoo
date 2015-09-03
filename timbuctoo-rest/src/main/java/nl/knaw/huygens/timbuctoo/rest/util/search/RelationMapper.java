package nl.knaw.huygens.timbuctoo.rest.util.search;

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

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Maps a list of homogeneous relations, i.e. with source type and target type
 * the same for all relations, to a list of {@code RelationDTO}s.
 */
public class RelationMapper {

  private final Repository repository;

  @Inject
  public RelationMapper(Repository repository) {
    this.repository = repository;
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
      Class<? extends DomainEntity> sourceType = vre.mapTypeName(relations.get(0).getSourceType(), true);
      Class<? extends DomainEntity> targetType = vre.mapTypeName(relations.get(0).getTargetType(), true);

      // Cache source entities: in a reception search we know they are likely to occur
      // multiple times, but we don't know the order in which they will be present.
      Map<String, DomainEntity> sources = Maps.newHashMap();

      for (Relation relation : relations) {
        RelationType relationType = repository.getRelationTypeById(relation.getTypeId(), true);
        String relationName = relationType.getRegularName();

        String sourceId = relation.getSourceId();
        DomainEntity source = sources.get(sourceId);
        if (source == null) {
          source = repository.getEntityOrDefaultVariationWithRelations(sourceType, sourceId);
          repository.addDerivedProperties(vre, source);
          sources.put(sourceId, source);
        }

        DomainEntity target = repository.getEntityOrDefaultVariationWithRelations(targetType, relation.getTargetId());
        repository.addDerivedProperties(vre, target);
        list.add(new RelationDTO(itype, xtype, relation.getId(), relationName, source, target));
      }
    }
    return list;
  }

}
