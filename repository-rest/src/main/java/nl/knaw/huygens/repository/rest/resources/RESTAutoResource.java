package nl.knaw.huygens.repository.rest.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
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

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.DomainEntity;
import nl.knaw.huygens.repository.search.SearchManager;
import nl.knaw.huygens.repository.storage.JsonViews;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

/**
 * A REST resource for adressing collections of documents.
 */
@Path("resources/{entityType: [a-zA-Z]+}")
public class RESTAutoResource {

  private final Logger LOG = LoggerFactory.getLogger(RESTAutoResource.class);

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: [a-zA-Z]{4}\\d+}";

  public static final String ENTITY_PARAM = "entityType";

  private final DocTypeRegistry docTypeRegistry;
  private final StorageManager storageManager;
  private final SearchManager searchManager;

  @Inject
  public RESTAutoResource(DocTypeRegistry registry, StorageManager storageManager, SearchManager searchManager) {
    docTypeRegistry = registry;
    this.storageManager = storageManager;
    this.searchManager = searchManager;
  }

  // --- API -----------------------------------------------------------

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends Entity> getAllDocs( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      @QueryParam("rows")
      @DefaultValue("200")
      int rows, //
      @QueryParam("start")
      int start //
  ) {
    Class<? extends Entity> type = getDocType(entityType);
    return storageManager.getAllLimited(type, start, rows);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> Response post( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      Entity input, //
      @Context
      UriInfo uriInfo //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);
    Class<? extends Entity> inputType = input.getClass();

    //TODO: find a better way to test if input type is the same as the entity type.
    //@see redmine issue #1419
    if (!type.isAssignableFrom(inputType) || !inputType.isAssignableFrom(type)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    T typedDoc = (T) input;
    storageManager.addEntity(type, typedDoc);

    String location = uriInfo.getBaseUri().toString() + "resources/" + entityType + "/" + input.getId();
    return Response.status(Status.CREATED).header("Location", location).build();
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public Entity getDoc( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      @PathParam(ID_PARAM)
      String id //
  ) {
    Class<? extends Entity> type = getDocType(entityType);
    Entity document = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);

    if (document instanceof DomainEntity) {
      try {
        searchManager.addRelationsTo((DomainEntity) document);
      } catch (SolrServerException e) {
        LOG.error(e.getMessage());
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    }
    return document;
  }

  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> void putDoc( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      @PathParam(ID_PARAM)
      String id, //
      Entity input //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);
    Class<? extends Entity> inputType = input.getClass();

    //TODO: find a better way to test if input type is the same as the entity type.
    //@see redmine issue #1419
    if (!type.isAssignableFrom(inputType) || !inputType.isAssignableFrom(type)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    try {
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.modifyEntity(type, typedDoc);
    } catch (IOException ex) {
      // only if the document version does not exist an IOException is thrown.
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @DELETE
  @Path(ID_PATH)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> Response delete( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      @PathParam(ID_PARAM)
      String id //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);
    T typedDoc = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    storageManager.removeEntity(type, typedDoc);
    return Response.status(Status.OK).build();
  }

  @GET
  @Path(ID_PATH + "/{variation: \\w+}")
  @JsonView(JsonViews.WebView.class)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public DomainEntity getDocOfVariation( //
      @PathParam(ENTITY_PARAM)
      String entityType, //
      @PathParam(ID_PARAM)
      String id, //
      @PathParam("variation")
      String variation //
  ) {
    @SuppressWarnings("unchecked")
    Class<? extends DomainEntity> type = (Class<? extends DomainEntity>) getDocType(entityType);
    return checkNotNull(storageManager.getCompleteVariation(type, id, variation), Status.NOT_FOUND);
  }

  // -------------------------------------------------------------------

  private Class<? extends Entity> getDocType(String entityType) {
    Class<? extends Entity> type = docTypeRegistry.getTypeForXName(entityType);
    if (type == null) {
      LOG.error("Cannot convert '{}' to a document type", entityType);
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return type;
  }

  /**
   * Checks the specified reference and throws a {@code WebApplicationException}
   * with the specified status if the reference is {@code null}.
   */
  private <T> T checkNotNull(T reference, Status status) {
    if (reference == null) {
      throw new WebApplicationException(status);
    }
    return reference;
  }

}
