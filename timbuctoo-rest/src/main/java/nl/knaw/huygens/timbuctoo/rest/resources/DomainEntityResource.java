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

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
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
@Path(Paths.DOMAIN_PREFIX + "/{entityName: [a-zA-Z]+}")
public class DomainEntityResource {

  private final Logger LOG = LoggerFactory.getLogger(DomainEntityResource.class);

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: [a-zA-Z]{4}\\d+}";

  public static final String ENTITY_PARAM = "entityName";

  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;
  private final PersistenceWrapper persistenceWrapper;

  @Inject
  public DomainEntityResource(TypeRegistry registry, StorageManager storageManager, PersistenceWrapper persistenceWrapper) {
    typeRegistry = registry;
    this.storageManager = storageManager;
    this.persistenceWrapper = persistenceWrapper;
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

    String id = storageManager.addEntity((Class<T>) type, (T) input);

    persistObject(type, id);

    String baseUri = CharMatcher.is('/').trimTrailingFrom(uriInfo.getBaseUri().toString());
    String location = Joiner.on('/').join(baseUri, Paths.DOMAIN_PREFIX, entityName, id);
    return Response.status(Status.CREATED).header("Location", location).build();
  }

  protected void persistObject(Class<? extends DomainEntity> type, String id) {
    try {
      String pid = persistenceWrapper.persistObject(typeRegistry.getINameForType(type), id);
      storageManager.setPID(type, id, pid);
    } catch (PersistenceException e) {
      LOG.error(String.format("Persisting of Object with id %s went wrong.", id), e);
    }
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
  public <T extends DomainEntity> void putDoc( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      DomainEntity input //
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    if (type != input.getClass()) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    try {
      checkWritable(input, Status.FORBIDDEN);
      storageManager.modifyEntity((Class<T>) type, (T) input);
    } catch (IOException e) {
      // only if the entity version does not exist an IOException is thrown.
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    persistObject(type, id);

  }

  @SuppressWarnings("unchecked")
  @DELETE
  @Path(ID_PATH)
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends DomainEntity> Response delete( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id //
  ) throws IOException {
    Class<? extends DomainEntity> type = getEntityType(entityName, Status.NOT_FOUND);
    DomainEntity typedDoc = checkNotNull(storageManager.getEntity(type, id), Status.NOT_FOUND);
    checkWritable(typedDoc, Status.FORBIDDEN);
    storageManager.removeEntity((Class<T>) type, (T) typedDoc);

    persistObject(type, id);
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
    return checkNotNull(storageManager.getCompleteVariation(type, id, variation), Status.NOT_FOUND);
  }

  // -------------------------------------------------------------------

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
   * Checks the specified reference and throws a {@code WebApplicationException}
   * with the specified status if the reference is {@code null}.
   */
  private <T> T checkNotNull(T reference, Status status) {
    if (reference == null) {
      throw new WebApplicationException(status);
    }
    return reference;
  }

  private void checkWritable(DomainEntity entity, Status status) {
    if (!(entity).isWritable()) {
      throw new WebApplicationException(status);
    }
  }

}
