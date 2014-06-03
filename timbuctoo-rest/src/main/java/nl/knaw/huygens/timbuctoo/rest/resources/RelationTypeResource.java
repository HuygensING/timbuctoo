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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/relationtypes")
public class RelationTypeResource extends ResourceBase {

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + RelationType.ID_PREFIX + "\\d+}";

  private final TypeRegistry registry;
  private final Repository repository;

  @Inject
  public RelationTypeResource(TypeRegistry registry, Repository repository) {
    this.registry = registry;
    this.repository = repository;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<RelationType> getRelationTypes(@QueryParam("iname") String name) {
    return getAvailableRelationTypes(name);
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public RelationType getRelationType(@PathParam(ID_PARAM) String id) {
    RelationType entity = repository.getEntity(RelationType.class, id);
    checkNotNull(entity, Status.NOT_FOUND, "No RelationType with id %s", id);
    return entity;
  }

  /**
   * Returns the relation types in which an entity with the specified internal name
   * can participate, either as "source" or as "target".
   * If {@code iname} is {@code null} or empty all relation types are returned.
   */
  @VisibleForTesting
  List<RelationType> getAvailableRelationTypes(String iname) {
    final String name = Strings.isNullOrEmpty(iname) ? null : mapName(iname);
    Predicate<RelationType> predicate = new Predicate<RelationType>() {
      @Override
      public boolean apply(RelationType entity) {
        return name == null || entity.hasSourceTypeName(name) || entity.hasTargetTypeName(name);
      }
    };
    List<RelationType> entities = repository.getSystemEntities(RelationType.class).getAll();
    return Lists.newArrayList(Iterables.filter(entities, predicate));
  }

  private String mapName(String iname) {
    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    checkNotNull(type, Status.BAD_REQUEST, "No DomainEntity with internal name %s", iname);
    return TypeNames.getInternalName(TypeRegistry.toBaseDomainEntity(type));
  }

}
