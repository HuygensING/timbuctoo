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

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.jms.JMSException;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

/**
 * A REST resource for adressing collections of domain entities.
 */
@Path(Paths.DOMAIN_PREFIX + "/{" + DomainEntityResource.ENTITY_PARAM + ": " + Paths.ENTITY_REGEX + "}")
public class DomainEntityResource extends ResourceBase {

  public static final String ENTITY_PARAM = "entityName";

  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityResource.class);

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + Paths.ID_REGEX + "}";
  private static final String PID_PATH = "/pid";

  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final Broker broker;
  private final VREManager vreManager;

  @Inject
  public DomainEntityResource(TypeRegistry registry, StorageManager storageManager, Broker broker, VREManager vreManager) {
    this.typeRegistry = registry;
    this.storageManager = storageManager;
    this.broker = broker;
    this.vreManager = vreManager;
  }

  // --- API -----------------------------------------------------------

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends DomainEntity> getAllDocs( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @QueryParam("rows") @DefaultValue("200") int rows, //
      @QueryParam("start") @DefaultValue("0") int start //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    return storageManager.getAllLimited(type, start, rows);
  }

  @SuppressWarnings("unchecked")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public <T extends DomainEntity> Response post( //
      @PathParam(ENTITY_PARAM) String entityName, //
      DomainEntity input, //
      @Context UriInfo uriInfo, //
      @HeaderParam(VRE_ID_KEY) String vreId, //
      @QueryParam(USER_ID_KEY) String userId//
  ) throws IOException, URISyntaxException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);

    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    checkCollectionInScope(type, vreId, Status.FORBIDDEN);

    Change change = new Change(userId, vreId);

    String id = storageManager.addDomainEntity((Class<T>) type, (T) input, change);
    notifyChange(ActionType.ADD, type, id);

    return Response.created(new URI(id)).build();
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public DomainEntity getDoc( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      @QueryParam(REVISION_KEY) Integer revision//
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);

    if (revision == null) {
      return checkNotNull(storageManager.getEntityWithRelations(type, id), Status.NOT_FOUND);
    } else {
      return checkNotNull(storageManager.getRevisionWithRelations(type, id, revision), Status.NOT_FOUND);
    }
  }

  @SuppressWarnings("unchecked")
  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public <T extends DomainEntity> void put( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      DomainEntity input, //
      @HeaderParam(VRE_ID_KEY) String vreId,//
      @QueryParam(USER_ID_KEY) String userId//
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);

    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    DomainEntity entity = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);

    checkItemInScope(type, id, vreId, Status.FORBIDDEN);

    checkWritable(entity, Status.FORBIDDEN);

    Change change = new Change(userId, vreId);

    try {
      if (TypeRegistry.isPrimitiveDomainEntity(type)) {
        storageManager.updatePrimitiveDomainEntity((Class<T>) type, (T) input, change);
      } else {
        storageManager.updateProjectDomainEntity((Class<T>) type, (T) input, change);
      }
    } catch (IOException e) {
      // TODO Handle two cases: 1)entity was already updated, 2) internal server error
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    notifyChange(ActionType.MOD, type, id);
  }

  @PUT
  @Path(PID_PATH)
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  public <T extends DomainEntity> void putPIDs(//
      @PathParam(ENTITY_PARAM) String entityName,//
      @HeaderParam(VRE_ID_KEY) String vreId) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getEntityType(entityName, Status.NOT_FOUND);

    if (DomainEntity.class.equals(type.getSuperclass())) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // if you want to be able to put a pid on items without pid you have to have access to the base class.
    checkCollectionInScope(TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type)), vreId, Status.FORBIDDEN);

    List<String> entityIds = storageManager.getAllIdsWithoutPIDOfType(type);

    for (T entity : storageManager.getAllByIds(type, entityIds)) {
      sendPersistMessage(ActionType.MOD, type, entity.getId());
    }

  }

  @DELETE
  @Path(ID_PATH)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public Response delete( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      @HeaderParam(VRE_ID_KEY) String vreId) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);

    DomainEntity entity = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    checkWritable(entity, Status.FORBIDDEN);

    checkItemInScope(type, id, vreId, Status.FORBIDDEN);

    storageManager.deleteDomainEntity(entity);
    notifyChange(ActionType.DEL, type, id);

    return Response.status(Status.NO_CONTENT).build();
  }

  // --- Message handling ----------------------------------------------

  public static final String INDEX_MSG_PRODUCER = "ResourceIndexProducer";
  public static final String PERSIST_MSG_PRODUCER = "ResourcePersistProducer";

  /**
   * Notify other software components of a change in the data.
   */
  private void notifyChange(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    switch (actionType) {
    case ADD:
    case MOD:
      sendPersistMessage(actionType, type, id);
      sendIndexMessage(actionType, type, id);
      break;
    case DEL:
      sendIndexMessage(actionType, type, id);
      break;
    default:
      LOG.error("Unexpected action {}", actionType);
      break;
    }
  }

  private void sendIndexMessage(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    try {
      Producer producer = broker.getProducer(INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE);
      producer.send(actionType, type, id);
    } catch (JMSException e) {
      LOG.error("Failed to send index message {} - {} - {}. \n{}", actionType, type, id, e.getMessage());
      LOG.debug("Exception", e);
    }
  }

  protected void sendPersistMessage(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    try {
      Producer producer = broker.getProducer(PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE);
      producer.send(actionType, type, id);
    } catch (JMSException e) {
      LOG.error("Failed to send persistence message {} - {} - {}. \n{}", actionType, type, id, e.getMessage());
      LOG.debug("Exception", e);
    }
  }

  // --- Conversion and Validation -------------------------------------

  private Class<? extends DomainEntity> getEntityType(String entityName, Status status) {
    Class<? extends Entity> type = typeRegistry.getTypeForXName(entityName);
    if (type != null && TypeRegistry.isDomainEntity(type)) {
      return TypeRegistry.toDomainEntity(type);
    } else {
      LOG.error("'{}' is not a domain entity name", entityName);
      throw new WebApplicationException(status);
    }
  }

  /**
   * Domain entities without a persistent identifier are not writable.
   * They are only available to check batch-imported data.
   */
  private void checkWritable(DomainEntity entity, Status status) {
    if (entity.getPid() == null) {
      LOG.info("Entity with id {} is not writeable", entity.getId());
      throw new WebApplicationException(status);
    }
  }

  /**
   * Helper method to check if the type is in the scope of the VRE.
   * @param type the type to check.
   * @param vreId the id of the VRE.
   */
  private <T extends DomainEntity> void checkCollectionInScope(Class<T> type, String vreId, Status status) {
    Scope scope = getScope(vreId);
    if (!scope.isTypeInScope(type)) {
      throw new WebApplicationException(status);
    }
  }

  /**
   * Helper method to check if the item is in the scope of the VRE. 
   * @param type the type of the item to check.
   * @param id the id of the item to check.
   * @param vreId the id of the VRE.
   */
  private <T extends DomainEntity> void checkItemInScope(Class<T> type, String id, String vreId, Status status) {
    Scope scope = getScope(vreId);
    if (!scope.inScope(type, id)) {
      throw new WebApplicationException(status);
    }
  }

  private Scope getScope(String vreId) {
    VRE vre = vreManager.getVREById(vreId);
    return vre.getScope();
  }

}
