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

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.graph.GraphBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

/**
 * Domain entities as graph.
 * 
 * Uses d3.js format:
 * {
 *   nodes: [
 *     {
 *       key: 'persons/PERS000000000042'
 *       type: 'person',
 *       label: 'John Doe',
 *     },
 *     ...
 *   ],
 *   links: [
 *     {
 *       source: 0,
 *       target: 2
 *       type: 'isParentOf'
 *     },
 *     ...
 *   ]
 * }
 */
@Path("graph/{" + GraphResource.ENTITY_PARAM + ": " + Paths.ENTITY_REGEX + "}")
public class GraphResource extends ResourceBase {

  public static final String ENTITY_PARAM = "entityName";

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + Paths.ID_REGEX + "}";

  private final Repository repository;
  private final TypeRegistry registry;

  @Inject
  public GraphResource(Repository repository) {
    this.repository = repository;
    registry = repository.getTypeRegistry();
  }

  // --- API -----------------------------------------------------------

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON })
  @JsonView(JsonViews.WebView.class)
  public Object getEntity(@PathParam(ENTITY_PARAM) String entityName, @PathParam(ID_PARAM) String id) {

    Class<? extends DomainEntity> type = registry.getTypeForXName(entityName);
    checkNotNull(type, NOT_FOUND, "No domain entity collection %s", entityName);

    DomainEntity entity = repository.getEntity(type, id);
    checkNotNull(entity, NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);

    try {
      GraphBuilder builder = new GraphBuilder(repository);
      builder.addEntity(entity);
      return builder.getGraph();
    } catch (Exception e) {
      throw new TimbuctooException(INTERNAL_SERVER_ERROR);
    }
  }

}
