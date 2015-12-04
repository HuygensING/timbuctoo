package nl.knaw.huygens.timbuctoo.graph;

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

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Creates a relation graph for visualization with d3.js.
 * <p/>
 * TODO Prevent duplicate links
 */
public class GraphBuilder {

  public static final Logger LOG = LoggerFactory.getLogger(GraphBuilder.class);
  private final Repository repository;
  private final VRE vre;
  private final TypeRegistry registry;

  private final D3Graph graph;

  public GraphBuilder(Repository repository, VRE vre) {
    this.repository = repository;
    this.vre = vre;
    registry = repository.getTypeRegistry();
    graph = new D3Graph();
  }

  public D3Graph getGraph() {
    return graph;
  }

  public <T extends DomainEntity> void addEntity(T entity, int depth) throws StorageException {
    addEntity(entity, depth, false, null);
  }

  public <T extends DomainEntity> void addEntity(T entity, int depth, List<String> types) throws StorageException {
    addEntity(entity, depth, false, types == null ? null : Sets.newHashSet(types));
  }

  private <T extends DomainEntity> void addEntity(T entity, int depth, boolean reduce, Set<String> types) throws StorageException {
    if (depth > 0 && accept(entity, reduce)) {
      String entityId = entity.getId();
      int index = addNodeToGraph(entity);

      for (Relation relation : repository.getRelationsByEntityId(entityId, 200)) {
        RelationType relationType = repository.getRelationTypeById(relation.getTypeId(), true);
        String name = relationType.getRegularName();
        if (types == null || types.size() == 0 || types.contains(name)) {
          if (relation.hasSourceId(entityId)) {
            DomainEntity other = getEntity(relation.getTargetType(), relation.getTargetId());
            int target = addNodeToGraph(other);
            graph.addLink(index, target, name);
            addEntity(other, depth - 1, true, types);
          } else {
            DomainEntity other = getEntity(relation.getSourceType(), relation.getSourceId());
            int source = addNodeToGraph(other);
            graph.addLink(source, index, name);
            addEntity(other, depth - 1, true, types);
          }
        }
      }
    }
  }

  private static Set<String> EXCLUDED = Sets.newHashSet("language", "location", "keyword");

  private boolean accept(DomainEntity entity, boolean reduce) {
    if (reduce) {
      String iname = TypeNames.getInternalName(entity.getClass());
      return !EXCLUDED.contains(iname);
    }
    return true;
  }

  private int addNodeToGraph(DomainEntity entity) {
    return graph.addNode(createNode(entity));
  }

  private <T extends DomainEntity> D3Node createNode(T entity) {
    @SuppressWarnings("unchecked")
    Class type = TypeRegistry.toBaseDomainEntity(entity.getClass());
    String key = TypeNames.getExternalName(type) + "/" + entity.getId();
    String iname = TypeNames.getInternalName(type);
    String label = entity.getIdentificationName();
    return new D3Node(key, iname, label);
  }

  private DomainEntity getEntity(String iname, String id) {
    Class<? extends DomainEntity> type = getType(iname);
    return repository.getEntityOrDefaultVariation(type, id);
  }

  private Class<? extends DomainEntity> getType(String iname) {
    try {
      return vre.mapTypeName(iname, true);
    } catch (IllegalStateException e) {
      LOG.info("Type |{}| could not be mapped in vre |{}|, defaulting to the primitive", iname, vre.getVreId());
      return registry.getDomainEntityType(iname);
    }

  }

}
