package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.index.IndexRequests;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.util.ClientIndexRequest;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;

@RolesAllowed({UserRoles.ADMIN_ROLE})
@Path(Paths.V2_1_PATH + Paths.ADMIN_PATH)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResourceV2_1 {

  @Context
  private UriInfo uriInfo;

  public static final String INDEX_PRODUCER = "IndexProducer";
  public static final Logger LOG = LoggerFactory.getLogger(AdminResourceV2_1.class);
  private final Broker broker;
  private final IndexRequests indexRequestStatus;
  private final TypeRegistry typeRegistry;
  private final VRECollection vreCollection;
  private final IndexRequestFactory indexRequestFactory;

  @Inject
  public AdminResourceV2_1(Broker broker, IndexRequests indexRequestStatus, TypeRegistry typeRegistry, VRECollection vreCollection, IndexRequestFactory indexRequestFactory) {
    this.broker = broker;
    this.indexRequestStatus = indexRequestStatus;
    this.typeRegistry = typeRegistry;
    this.vreCollection = vreCollection;
    this.indexRequestFactory = indexRequestFactory;
  }

  @POST
  @Path(Paths.INDEX_REQUEST_PATH)
  public Response postIndexRequest(ClientIndexRequest clientRequest, @HeaderParam(VRE_ID_KEY) String vreId) {
    String collectionName = clientRequest.getCollectionName();
    if (collectionName == null) {

      return Response.status(BAD_REQUEST).entity(new ExceptionMessage("\"type\" cannot be null")).build();
    }

    Class<? extends DomainEntity> type = typeRegistry.getTypeForXName(collectionName);

    if (type == null) {
      return Response.status(BAD_REQUEST).entity(new ExceptionMessage(String.format("[%s] is not a valid collection.", collectionName))).build();
    }


    VRE vre = vreCollection.getVREById(vreId);
    if (!vre.inScope(type)) {
      return Response.status(FORBIDDEN).entity(new ExceptionMessage(String.format("[%s] is not in scopr of [%s].", collectionName, vreId))).build();
    }

    try {
      Producer producer = broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE);

      String id = indexRequestStatus.add(indexRequestFactory.forType(type));

      producer.send(Action.forRequestWithId(ActionType.MOD, id));

      URI location = uriInfo.getAbsolutePathBuilder().path(id).build();
      LOG.info("location: [{}]", location);
      return Response.created(location).build();

    } catch (JMSException e) {
      LOG.error("Could not get producer with name [{}] and queue [{}]", INDEX_PRODUCER, INDEX_QUEUE);
      LOG.error("Exception thrown", e);
      return Response.serverError().entity("Could not handle request.").build();
    }
  }

  @GET
  @Path(Paths.INDEX_REQUEST_PATH + Paths.INDEX_REQUEST_ID_VALUE_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIndexRequest(@PathParam(Paths.ID_PARAM) String id, @HeaderParam(VRE_ID_KEY) String vreId) {
    LOG.info("id: " + id);

    IndexRequest indexRequest = indexRequestStatus.get(id);
    if (indexRequest == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    VRE vre = vreCollection.getVREById(vreId);
    Class<? extends DomainEntity> type = indexRequest.getType();
    if (!vre.inScope(type)) {
      return Response.status(FORBIDDEN).entity(new ExceptionMessage(String.format("[%s] is not in scopr of [%s].", TypeNames.getExternalName(type), vreId))).build();
    }

    return Response.ok(indexRequest.toClientRep()).build();
  }

  private class ExceptionMessage {
    private String message;

    ExceptionMessage(String message) {
      this.message = message;
    }

    public ExceptionMessage() {

    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
