package nl.knaw.huygens.timbuctoo.graph;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationRef;

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

/**
 * Creates an entity graph for visualization.
 */
public class EntityGraph {

  private final D3Graph graph;

  public EntityGraph() {
    graph = new D3Graph();
  }

  public D3Graph getGraph() {
    return graph;
  }

  public <T extends DomainEntity> void addEntity(T entity) {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) entity.getClass();
    int source = graph.addNode(createNode(type, entity));
    for (Map.Entry<String, List<RelationRef>> entry : entity.getRelations().entrySet()) {
      for (RelationRef ref : entry.getValue()) {
        int target = graph.addNode(createNode(ref));
        graph.addLink(createLink(source, target, entry.getKey()));
      }
    }
  }

  private <T extends DomainEntity> D3Node createNode(Class<T> type, T entity) {
    String key = TypeNames.getExternalName(type) + "/" + entity.getId();
    String iname = TypeNames.getInternalName(type);
    String label = entity.getDisplayName();
    return new D3Node(key, iname, label);
  }

  private D3Node createNode(RelationRef ref) {
    String path = ref.getPath();
    String key = path.substring(path.indexOf('/') + 1);
    String iname = ref.getType();
    String label = ref.getDisplayName();
    return new D3Node(key, iname, label);
  }

  private D3Link createLink(int source, int target, String type) {
    return new D3Link(source, target, type);
  }

}
