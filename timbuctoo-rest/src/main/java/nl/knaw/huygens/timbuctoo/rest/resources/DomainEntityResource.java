package nl.knaw.huygens.timbuctoo.rest.resources;

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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.config.Paths.DOMAIN_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.PID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.UPDATE_PID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V1_PATH_OPTIONAL;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;

/**
 * A REST resource for addressing collections of domain entities.
 */
@Path(V1_PATH_OPTIONAL + DOMAIN_PREFIX + "/" + ENTITY_PATH)
public class DomainEntityResource extends ResourceBase {

  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityResource.class);

  protected final TypeRegistry typeRegistry;
  protected final ChangeHelper changeHelper;
  private final PersistenceRequestFactory persistenceRequestFactory;

  @Inject
  public DomainEntityResource(TypeRegistry registry, Repository repository, ChangeHelper changeHelper, VRECollection vreCollection, PersistenceRequestFactory persistenceRequestFactory) {
    super(repository, vreCollection);
    this.typeRegistry = registry;
    this.changeHelper = changeHelper;
    this.persistenceRequestFactory = persistenceRequestFactory;
  }

  // --- API -----------------------------------------------------------

  @APIDesc("Get an number of entities. Query params: \"rows\" (default: 200) and \"start\" (default: 0).")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response getEntities( //
                               @PathParam(ENTITY_PARAM) String entityName, //
                               @QueryParam("type") String typeValue, //
                               @QueryParam("rows") @DefaultValue("200") int rows, //
                               @QueryParam("start") @DefaultValue("0") int start //
  ) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    LOG.info("Begin retrieving entities: [{}]", entityName);
    Class<? extends DomainEntity> entityType = getValidEntityType(entityName);
    List<? extends DomainEntity> list = retrieveEntities(entityType, typeValue, rows, start);
    LOG.info("Done retrievingEntities: [{}] in [{}]", entityName, stopwatch.stop());
    return Response.ok(new GenericEntity<List<? extends DomainEntity>>(list) {
    }).build();
  }

  protected final <T extends DomainEntity> List<T> retrieveEntities(Class<T> entityType, String typeValue, int rows, int start) {
    if (Strings.isNullOrEmpty(typeValue)) {
      return repository.getDomainEntities(entityType).skip(start).getSome(rows);
    } else {
      // used for filtering keywords
      return repository.getEntitiesByProperty(entityType, "type", typeValue).getAll();
    }
  }

  @APIDesc("Post an entity. Body required.")
  @SuppressWarnings("unchecked")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({USER_ROLE, ADMIN_ROLE})
  public <T extends DomainEntity> Response post( //
                                                 @PathParam(ENTITY_PARAM) String entityName, //
                                                 DomainEntity input, //
                                                 @Context UriInfo uriInfo, //
                                                 @HeaderParam(VRE_ID_KEY) String vreId, //
                                                 @QueryParam(USER_ID_KEY) String userId//
  ) throws StorageException, URISyntaxException {

    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    checkCondition(type == input.getClass(), BAD_REQUEST, "Type %s does not match input", type.getSimpleName());

    VRE vre = getValidVRE(vreId);
    isInScope(vreId, vre, type);

    Change change = new Change(userId, vreId);

    String id = null;
    try {
      id = repository.addDomainEntity((Class<T>) type, (T) input, change);
    } catch (DuplicateException e) {
      // TODO find a better solution
      LOG.info("Duplicate entity {} with id {}", entityName, e.getDuplicateId());
      id = updateTheDuplicateEntity(entityName, input, vreId, userId, e.getDuplicateId());
    } catch (ValidationException e) {
      throw new TimbuctooException(BAD_REQUEST, "Invalid entity; %s", e.getMessage());
    }
    changeHelper.notifyChange(ActionType.ADD, type, input, id);

    return Response.created(new URI(id)).build();
  }

  private String updateTheDuplicateEntity(String entityName, DomainEntity input, String vreId, String userId, String id) throws StorageException {
    Class<? extends DomainEntity> entityType = getValidEntityType(entityName);
    DomainEntity duplicatEnity = repository.getEntityOrDefaultVariation(entityType, id);

    input.setRev(duplicatEnity.getRev());
    input.setId(id);

    put(entityName, id, input, vreId, userId);
    return id;
  }

  @APIDesc("Get a single entity. Query param: \"rev\" (default:latest) ")
  @GET
  @Path(ID_PATH)
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public DomainEntity getDoc( //
                              @PathParam(ENTITY_PARAM) String entityName, //
                              @PathParam(ID_PARAM) String id, //
                              @QueryParam(REVISION_KEY) Integer revision//
  ) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);

    if (revision == null) {
      DomainEntity entity = repository.getEntityOrDefaultVariationWithRelations(type, id);
      return checkNotNull(entity, NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);
    } else {
      DomainEntity entity = repository.getRevisionWithRelations(type, id, revision);
      return checkNotNull(entity, NOT_FOUND, "No %s with id %s and revision %s", type.getSimpleName(), id, revision);
    }
  }

  @APIDesc("Update an entity. Body required.")
  @SuppressWarnings("unchecked")
  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({USER_ROLE, ADMIN_ROLE})
  public <T extends DomainEntity> Response put( //
                                                @PathParam(ENTITY_PARAM) String entityName, //
                                                @PathParam(ID_PARAM) String id, //
                                                DomainEntity input, //
                                                @HeaderParam(VRE_ID_KEY) String vreId,//
                                                @QueryParam(USER_ID_KEY) String userId//
  ) {

    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    checkCondition(type == input.getClass(), BAD_REQUEST, "Type %s does not match input", type.getSimpleName());

    DomainEntity entity = repository.getEntityOrDefaultVariation(type, id);
    checkNotNull(entity, NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);
    checkNotNull(entity.getPid(), FORBIDDEN, "%s with id %s is read-only (no PID)", type.getSimpleName(), id);

    VRE vre = getValidVRE(vreId);
    checkCondition(vre.inScope(type, id), FORBIDDEN, "Entity %s %s not in scope %s", type, id, vreId);

    try {
      Change change = new Change(userId, vreId);
      repository.updateDomainEntity((Class<T>) type, (T) input, change);
      changeHelper.notifyChange(ActionType.MOD, type, entity, id);
    } catch (NoSuchEntityException e) {
      throw new TimbuctooException(NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);
    } catch (UpdateException e) {
      throw new TimbuctooException(Status.CONFLICT, "Entity %s with id %s already updated", type.getSimpleName(), id);
    } catch (StorageException e) {
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
    return Response.noContent().build();
  }

  @APIDesc("Set the pids of the entities.")
  @PUT
  @Path(PID_PATH)
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.APPLICATION_JSON)
  public void putPIDs(//
                      @PathParam(ENTITY_PARAM) String entityName,//
                      @HeaderParam(VRE_ID_KEY) String vreId) {

    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new TimbuctooException(BAD_REQUEST, "Illegal PUT for primitive domain entity %s", type.getSimpleName());
    }

    // to put a pid you must have access to the base class
    VRE vre = getValidVRE(vreId);
    Class<? extends DomainEntity> base = TypeRegistry.toBaseDomainEntity(type);
    isInScope(vreId, vre, base);

    try {
      for (String id : repository.getAllIdsWithoutPID(type)) {
        changeHelper.sendPersistMessage(persistenceRequestFactory.forEntity(ActionType.ADD, type, id));
      }
    } catch (StorageException e) {
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  private void isInScope(@HeaderParam(VRE_ID_KEY) String vreId, VRE vre, Class<? extends DomainEntity> base) {
    checkCondition(vre.inScope(base), FORBIDDEN, "Type %s not in scope %s", base, vreId);
  }

  @APIDesc("Update the pids of the entities with one")
  @PUT
  @Path(UPDATE_PID_PATH)
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.APPLICATION_JSON)
  public void updatePIDs(@PathParam(ENTITY_PARAM) String entityName, @HeaderParam(VRE_ID_KEY) String vredId) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);

    VRE vre = getValidVRE(vredId);
    Class<? extends DomainEntity> baseType = TypeRegistry.toBaseDomainEntity(type);
    isInScope(vredId, vre, baseType);

    changeHelper.sendPersistMessage(persistenceRequestFactory.forCollection(ActionType.MOD, type));
  }

  @APIDesc("Delete an specific entity.")
  @DELETE
  @Path(ID_PATH)
  @RolesAllowed({USER_ROLE, ADMIN_ROLE})
  public Response delete( //
                          @PathParam(ENTITY_PARAM) String entityName, //
                          @PathParam(ID_PARAM) String id, //
                          @HeaderParam(VRE_ID_KEY) String vreId) {

    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    if (!TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new TimbuctooException(BAD_REQUEST, "Not a primitive domain entity: %s", entityName);
    }

    DomainEntity entity = repository.getEntityOrDefaultVariation(type, id);
    checkNotNull(entity, NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);
    checkNotNull(entity.getPid(), FORBIDDEN, "%s with id %s is read-only (no PID)", type, id);

    VRE vre = getValidVRE(vreId);
    checkCondition(vre.inScope(type, id), FORBIDDEN, "%s with id %s not in scope %s", type, id, vreId);

    try {
      repository.deleteDomainEntity(entity);
      changeHelper.notifyChange(ActionType.DEL, type, entity, id);
      return Response.status(Status.NO_CONTENT).build();
    } catch (NoSuchEntityException e) {
      throw new TimbuctooException(NOT_FOUND, "No %s with id %s", type.getSimpleName(), id);
    } catch (StorageException e) {
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------

  protected final Class<? extends DomainEntity> getValidEntityType(String name) {
    return checkNotNull(typeRegistry.getTypeForXName(name), NOT_FOUND, "No domain entity collection %s", name);
  }
}
