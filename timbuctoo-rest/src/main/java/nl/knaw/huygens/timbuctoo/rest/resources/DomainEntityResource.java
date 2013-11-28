package nl.knaw.huygens.timbuctoo.rest.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

/**
 * A REST resource for adressing collections of domain entities.
 */
@Path(Paths.DOMAIN_PREFIX + "/{" + DomainEntityResource.ENTITY_PARAM + ": [a-zA-Z]+}")
public class DomainEntityResource extends ResourceBase {

  public static final String ENTITY_PARAM = "entityName";

  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityResource.class);

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: [a-zA-Z]{4}\\d+}";

  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final Broker broker;

  @Inject
  public DomainEntityResource(TypeRegistry registry, StorageManager storageManager, Broker broker) {
    this.typeRegistry = registry;
    this.storageManager = storageManager;
    this.broker = broker;
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
  @RolesAllowed("USER")
  public <T extends DomainEntity> Response post( //
      @PathParam(ENTITY_PARAM) String entityName, //
      DomainEntity input, //
      @Context UriInfo uriInfo //
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    String id = storageManager.addDomainEntity((Class<T>) type, (T) input);
    notifyChange(ActionType.ADD, type, id);

    String baseUri = CharMatcher.is('/').trimTrailingFrom(uriInfo.getBaseUri().toString());
    String location = Joiner.on('/').join(baseUri, Paths.DOMAIN_PREFIX, entityName, id);
    return Response.status(Status.CREATED).header("Location", location).build();
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public DomainEntity getDoc( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    return checkNotNull(storageManager.getEntityWithRelations(type, id), Status.NOT_FOUND);
  }

  @SuppressWarnings("unchecked")
  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends DomainEntity> void put( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      DomainEntity input //
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    DomainEntity entity = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    checkWritable(entity, Status.FORBIDDEN);

    try {
      storageManager.modifyEntity((Class<T>) type, (T) input);
    } catch (IOException e) {
      // TODO improve the logic, we already have checked existnce
      // storage manager should no throw an exception
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    notifyChange(ActionType.MOD, type, id);
  }

  @DELETE
  @Path(ID_PATH)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public Response delete( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id //
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);

    DomainEntity entity = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    checkWritable(entity, Status.FORBIDDEN);

    storageManager.removeEntity(entity);
    notifyChange(ActionType.DEL, type, id);

    return Response.status(Status.NO_CONTENT).build();
  }

  @GET
  @Path(ID_PATH + "/{variation: \\w+}")
  @JsonView(JsonViews.WebView.class)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public DomainEntity getDocOfVariation( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      @PathParam("variation") String variation //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    return checkNotNull(storageManager.getVariation(type, id, variation), Status.NOT_FOUND);
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

}
