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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Browser;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PERSIST_QUEUE;
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
  private final TypeRegistry typeRegistry;
  private final VRECollection vreCollection;
  private final IndexRequestFactory indexRequestFactory;

  @Inject
  public AdminResourceV2_1(Broker broker, TypeRegistry typeRegistry, VRECollection vreCollection, IndexRequestFactory indexRequestFactory) {
    this.broker = broker;
    this.typeRegistry = typeRegistry;
    this.vreCollection = vreCollection;
    this.indexRequestFactory = indexRequestFactory;
  }

  @POST
  @Path(Paths.INDEX_REQUEST_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response indexCollection(ClientIndexRequest clientRequest, @HeaderParam(VRE_ID_KEY) String vreId) {
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

      IndexRequest request = indexRequestFactory.forCollectionOf(ActionType.MOD, type);

      producer.send(request.toAction());

      return Response.ok().build();

    } catch (JMSException e) {
      LOG.error("Could not get producer with name [{}] and queue [{}]", INDEX_PRODUCER, INDEX_QUEUE);
      LOG.error("Exception thrown", e);
      return Response.serverError().entity("Could not handle request.").build();
    }
  }

  @GET
  @Path("persistencequeue")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPersistence() {
    try {
      Browser browser = broker.newBrowser(PERSIST_QUEUE);

      String status = browser.status();

      browser.close();
      return Response.ok().entity(status).build();
    } catch (JMSException e) {
      LOG.error("Could not get browser for queue [{}]", PERSIST_QUEUE);
      LOG.error("Exception thrown", e);
      return Response.serverError().entity("Could not retrieve persistence queue").build();
    }
  }

  @GET
  @Path("indexqueue")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIndex() {
    try {
      Browser browser = broker.newBrowser(INDEX_QUEUE);

      String status = browser.status();

      browser.close();
      return Response.ok().entity(status).build();
    } catch (JMSException e) {
      LOG.error("Could not get browser for queue [{}]", INDEX_QUEUE);
      LOG.error("Exception thrown", e);
      return Response.serverError().entity("Could not retrieve index queue").build();
    }
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
