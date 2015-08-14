package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.IndexRequestStatus;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;

@Path(Paths.V2_1_PATH + Paths.ADMIN_PATH)
public class AdminResourceV2_1 {

  @Context
  private UriInfo uriInfo;

  public static final String INDEX_PRODUCER = "IndexProducer";
  public static final Logger LOG = LoggerFactory.getLogger(AdminResourceV2_1.class);
  private final Broker broker;
  private final IndexRequestStatus indexRequestStatus;
  private final TypeRegistry typeRegistry;

  @Inject
  public AdminResourceV2_1(Broker broker, IndexRequestStatus indexRequestStatus, TypeRegistry typeRegistry) {
    this.broker = broker;
    this.indexRequestStatus = indexRequestStatus;
    this.typeRegistry = typeRegistry;
  }

  @POST
  @Path(Paths.INDEX_REQUEST_PATH)
  public Response postIndexRequest() {
    try {
      Producer producer = broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE);

      for (Class<? extends DomainEntity> type : typeRegistry.getPrimitiveDomainEntityTypes()) {
        producer.send(Action.multiUpdateActionFor(type));
      }

      String id = indexRequestStatus.add(IndexRequest.indexAll());

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
  @Path(Paths.INDEX_REQUEST_PATH + Paths.ID_PATH)
  public Response getIndexRequest(@PathParam(Paths.ID_PARAM) String id){
    IndexRequest indexRequest = indexRequestStatus.get(id);
    if(indexRequest == null){
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(indexRequest.toClientRep()).build();
  }
}
