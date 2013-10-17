package nl.knaw.huygens.timbuctoo.rest.resources;

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

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

/**
 * A REST resource for adressing collections of domain entities.
 */
@Path(Paths.DOMAIN_PREFIX + "/{entityName: [a-zA-Z]+}")
public class RESTAutoResource {

  private final Logger LOG = LoggerFactory.getLogger(RESTAutoResource.class);

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: [a-zA-Z]{4}\\d+}";

  public static final String ENTITY_PARAM = "entityName";

  private final DocTypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final SearchManager searchManager;

  @Inject
  public RESTAutoResource(DocTypeRegistry registry, StorageManager storageManager, SearchManager searchManager) {
    typeRegistry = registry;
    this.storageManager = storageManager;
    this.searchManager = searchManager;
  }

  // --- API -----------------------------------------------------------

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends DomainEntity> getAllDocs( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      @QueryParam("rows")
      @DefaultValue("200")
      int rows, //
      @QueryParam("start")
      int start //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    return storageManager.getAllLimited(type, start, rows);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> Response post( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      Entity input, //
      @Context
      UriInfo uriInfo //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityName);
    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    T typedDoc = (T) input;
    storageManager.addEntity(type, typedDoc);

    String baseUri = CharMatcher.is('/').trimTrailingFrom(uriInfo.getBaseUri().toString());
    String location = Joiner.on('/').join(baseUri, Paths.DOMAIN_PREFIX, entityName, input.getId());
    return Response.status(Status.CREATED).header("Location", location).build();
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public DomainEntity getDoc( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      @PathParam(ID_PARAM)
      String id //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    DomainEntity entity = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);

    try {
      searchManager.addRelationsTo((DomainEntity) entity);
    } catch (SolrServerException e) {
      LOG.error(e.getMessage());
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return entity;
  }

  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> void putDoc( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      @PathParam(ID_PARAM)
      String id, //
      Entity input //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityName);
    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    try {
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      checkWritable(typedDoc, Status.FORBIDDEN);
      storageManager.modifyEntity(type, typedDoc);
    } catch (IOException ex) {
      // only if the entity version does not exist an IOException is thrown.
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @DELETE
  @Path(ID_PATH)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Entity> Response delete( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      @PathParam(ID_PARAM)
      String id //
  ) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityName);
    T typedDoc = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    checkWritable(typedDoc, Status.FORBIDDEN);
    storageManager.removeEntity(type, typedDoc);
    return Response.status(Status.NO_CONTENT).build();
  }

  @GET
  @Path(ID_PATH + "/{variation: \\w+}")
  @JsonView(JsonViews.WebView.class)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public DomainEntity getDocOfVariation( //
      @PathParam(ENTITY_PARAM)
      String entityName, //
      @PathParam(ID_PARAM)
      String id, //
      @PathParam("variation")
      String variation //
  ) {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    return checkNotNull(storageManager.getCompleteVariation(type, id, variation), Status.NOT_FOUND);
  }

  // -------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private Class<? extends DomainEntity> getEntityType(String entityName, Status status) {
    Class<? extends Entity> type = typeRegistry.getTypeForXName(entityName);
    if (type != null && DomainEntity.class.isAssignableFrom(type)) {
      return (Class<? extends DomainEntity>) type;
    } else {
      LOG.error("'{}' is not a domain entity name", entityName);
      throw new WebApplicationException(status);
    }
  }

  private Class<? extends Entity> getDocType(String entityName) {
    Class<? extends Entity> type = typeRegistry.getTypeForXName(entityName);
    if (type == null) {
      LOG.error("Cannot convert '{}' to a entity type", entityName);
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

  private <T extends Entity> void checkWritable(T reference, Status status) {
    if (reference instanceof SystemEntity) {

    } else if (reference instanceof DomainEntity) {
      if (!((DomainEntity) reference).isWritable()) {
        throw new WebApplicationException(status);
      }
    } else {
      throw new WebApplicationException(status);
    }
  }

}
