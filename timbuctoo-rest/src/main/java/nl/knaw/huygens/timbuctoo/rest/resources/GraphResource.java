package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Domain entities as graph.
 * 
 * Uses d3.js format:
 * {
 *   fullNodeCount: 42,
 *   nodes: [
 *     {
 *       name: 'John Doe',
 *       type: 'person',
 *       path: 'persons/PERS000000000042'
 *     },
 *     ...
 *   ],
 *   links: [
 *     {
 *       source: 0,
 *       target: 2
 *       type: 'isParentOf'
 *     }
 *   ]
 * }
 */
@Path("graph/{" + GraphResource.ENTITY_PARAM + ": " + Paths.ENTITY_REGEX + "}")
public class GraphResource extends ResourceBase {

  public static final String ENTITY_PARAM = "entityName";

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + Paths.ID_REGEX + "}";

  private final Repository repository;
  private final TypeRegistry typeRegistry;

  @Inject
  public GraphResource(Repository repository) {
    this.repository = repository;
    typeRegistry = repository.getTypeRegistry();
  }

  // --- API -----------------------------------------------------------

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON })
  @JsonView(JsonViews.WebView.class)
  public Object getEntity( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);

    DomainEntity entity = repository.getEntityWithRelations(type, id);
    checkNotNull(entity, NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);

    Map<String, Object> result = Maps.newHashMap();
    result.put("fullNodeCount", entity.getRelationCount() + 1);
    List<Map<String, Object>> nodes = Lists.newArrayList();
    result.put("nodes", nodes);
    List<Map<String, Object>> links = Lists.newArrayList();
    result.put("links", links);

    nodes.add(createNode(entity));

    int index = 1;
    for (Map.Entry<String, List<RelationRef>> entry : entity.getRelations().entrySet()) {
      List<RelationRef> refs = entry.getValue();
      for (RelationRef ref : refs) {
        nodes.add(createNode(ref));
        Map<String, Object> link = Maps.newHashMap();
        link.put("source", 0);
        link.put("target", index++);
        link.put("type", entry.getKey());
        links.add(link);
      }
    }
    return result;
  }

  private Map<String, Object> createNode(DomainEntity entity) {
    Map<String, Object> node = Maps.newHashMap();
    node.put("name", entity.getDisplayName());
    node.put("type", TypeNames.getInternalName(entity.getClass()));
    node.put("path", TypeNames.getExternalName(entity.getClass()) + "/" + entity.getId());
    return node;
  }

  private Map<String, Object> createNode(RelationRef ref) {
    Map<String, Object> node = Maps.newHashMap();
    node.put("name", ref.getDisplayName());
    node.put("type", ref.getType());
    String path = ref.getPath();
    node.put("path", path.substring(path.indexOf('/') + 1));
    return node;
  }

  // ---------------------------------------------------------------------------

  private Class<? extends DomainEntity> getValidEntityType(String name) {
    Class<? extends DomainEntity> type = typeRegistry.getTypeForXName(name);
    checkNotNull(type, NOT_FOUND, "No domain entity collection %s", name);
    return type;
  }

}
