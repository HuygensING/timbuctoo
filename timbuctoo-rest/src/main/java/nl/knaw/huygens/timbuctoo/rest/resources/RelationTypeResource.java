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

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/relationtypes")
public class RelationTypeResource extends ResourceBase {

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + RelationType.ID_PREFIX + "\\d+}";

  private final TypeRegistry registry;
  private final StorageManager storageManager;

  @Inject
  public RelationTypeResource(TypeRegistry registry, StorageManager storageManager) {
    this.registry = registry;
    this.storageManager = storageManager;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<RelationType> getRelationTypes(@QueryParam("iname") String name) {
    return getRelationTypesForEntity(name);
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public RelationType getRelationType(@PathParam(ID_PARAM) String id) {
    return checkNotNull(storageManager.getEntity(RelationType.class, id), Status.NOT_FOUND);
  }

  /**
   * Returns the relation types in which an entity with the specified internal name
   * can participate, either as "source" or as "target".
   * If {@code iname} is {@code null} or empty all relation types are returned.
   */
  protected List<RelationType> getRelationTypesForEntity(String iname) {
    boolean showAll = Strings.isNullOrEmpty(iname);
    List<RelationType> types = Lists.newArrayList();
    for (RelationType type : storageManager.getAll(RelationType.class).getAll()) {
      if (showAll || isApplicable(iname, type)) {
        types.add(type);
      }
    }
    return types;
  }

  protected boolean isApplicable(String iname, RelationType type) {
    Class<? extends DomainEntity> requestType = TypeRegistry.toDomainEntity(convertToType(iname));
    Class<? extends DomainEntity> sourceType = TypeRegistry.toDomainEntity(convertToType(type.getSourceTypeName()));
    Class<? extends DomainEntity> targetType = TypeRegistry.toDomainEntity(convertToType(type.getTargetTypeName()));

    // iname is assignable from source or target of relation
    boolean isAssignable = isAssignable(sourceType, requestType) || isAssignable(targetType, requestType);

    boolean isSourceCompatible = isCompatible(requestType, sourceType);
    boolean isTargetCompatible = isCompatible(requestType, targetType);

    boolean isRequestTypePrimitive = TypeRegistry.isPrimitiveDomainEntity(requestType);

    boolean isPrimitiveCompatible = isRequestTypePrimitive && isAssignable && (isSourceCompatible || isTargetCompatible);
    boolean isCompatibleForProjectType = isAssignable && isSourceCompatible && isTargetCompatible;

    return isPrimitiveCompatible || isCompatibleForProjectType;
  }

  private Class<? extends Entity> convertToType(String iname) {
    return "domainentity".equals(iname) ? DomainEntity.class : registry.getTypeForIName(iname);
  }

  private boolean isCompatible(Class<? extends DomainEntity> requestType, Class<? extends DomainEntity> typeFromRelation) {
    return registry.isFromSameProject(requestType, typeFromRelation) || //
      TypeRegistry.isPrimitiveDomainEntity(requestType) || // 
      TypeRegistry.isPrimitiveDomainEntity(typeFromRelation) || //
      DomainEntity.class.equals(requestType) || //
      DomainEntity.class.equals(typeFromRelation);
  }
  /**
   * Convenience method for deciding assignability of an entity to another entity,
   * given the internal names of the target entity type and the source entity type.
   */
  private boolean isAssignable(Class<? extends DomainEntity> targetType, Class<? extends DomainEntity> sourceType) {
    return targetType != null && sourceType != null && targetType.isAssignableFrom(sourceType);
  }

}
