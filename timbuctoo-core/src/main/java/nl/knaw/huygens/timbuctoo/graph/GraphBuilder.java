package nl.knaw.huygens.timbuctoo.graph;

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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

/**
 * Creates a relation graph for visualization with d3.js.
 */
public class GraphBuilder {

  private final Repository repository;
  private final TypeRegistry registry;

  private final D3Graph graph;

  public GraphBuilder(Repository repository) {
    this.repository = repository;
    registry = repository.getTypeRegistry();
    graph = new D3Graph();
  }

  public D3Graph getGraph() {
    return graph;
  }

  public <T extends DomainEntity> void addEntity(T entity) throws StorageException {
    String entityId = entity.getId();
    int index = graph.addNode(createNode(entity));

    for (Relation relation : repository.getRelationsByEntityId(entityId, 200)) {
      RelationType relationType = repository.getRelationType(relation.getTypeId());
      String name = relationType.getRegularName();
      if (relation.hasSourceId(entityId)) {
        D3Node node = createNode(relation.getTargetType(), relation.getTargetId());
        int target = graph.addNode(node);
        graph.addLink(index, target, name);
      } else {
        D3Node node = createNode(relation.getSourceType(), relation.getSourceId());
        int source = graph.addNode(node);
        graph.addLink(source, index, name);
      }
    }
  }

  private D3Node createNode(String iname, String id) {
    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    return createNode(repository.getEntity(type, id));
  }

  private <T extends DomainEntity> D3Node createNode(T entity) {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) entity.getClass();
    String key = TypeNames.getExternalName(type) + "/" + entity.getId();
    String iname = TypeNames.getInternalName(type);
    String label = entity.getDisplayName();
    return new D3Node(key, iname, label);
  }

}
