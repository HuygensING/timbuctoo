package nl.knaw.huygens.repository.resources;

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
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.storage.JsonViews;
import nl.knaw.huygens.repository.storage.StorageManager;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("resources/{entityType: [a-zA-Z]+}")
public class RESTAutoResource {

  private static final String ID_PARAM = "id";

  public static final String ENTITY_PARAM = "entityType";

  private final StorageManager storageManager;
  private final DocTypeRegistry docTypeRegistry;

  @Inject
  public RESTAutoResource(StorageManager manager, DocTypeRegistry registry) {
    storageManager = manager;
    docTypeRegistry = registry;
  }

  @GET
  @Path("/all")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends Document> getAllDocs(@PathParam(ENTITY_PARAM) String entityType, @QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    Class<? extends Document> type = getDocType(entityType);
    return storageManager.getAllLimited(type, start, rows);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/all")
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Document> Response post(@PathParam(ENTITY_PARAM) String entityType, Document input, @Context UriInfo uriInfo) throws IOException {

    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);

    Class<? extends Document> inputType = input.getClass();

    //TODO: find a better way to test if input type is the same as the entity type.
    //@see redmine issue #1419
    if (!type.isAssignableFrom(inputType) || !inputType.isAssignableFrom(type)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    T typedDoc = (T) input;
    storageManager.addDocument(type, typedDoc);

    String location = uriInfo.getBaseUri().toString() + "resources/" + entityType + "/" + input.getId();
    return Response.status(Response.Status.CREATED).header("Location", location).build();
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public Document getDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id) {
    Class<? extends Document> type = getDocType(entityType);
    Document doc = storageManager.getCompleteDocument(type, id);
    if (doc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return doc;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Document> void putDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id, Document input) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);
    Class<? extends Document> inputType = input.getClass();

    //TODO: find a better way to test if input type is the same as the entity type.
    //@see redmine issue #1419
    if (!type.isAssignableFrom(inputType) || !inputType.isAssignableFrom(type)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    try {
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.modifyDocument(type, typedDoc);
    } catch (IOException ex) {
      // only if the document version does not exist an IOException is thrown.
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

  }

  @DELETE
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public <T extends Document> Response delete(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id) throws IOException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) getDocType(entityType);
    T typedDoc = storageManager.getDocument(type, id);

    if (typedDoc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    storageManager.removeDocument(type, typedDoc);

    return Response.status(Response.Status.OK).build();
  }

  @GET
  @JsonView(JsonViews.WebView.class)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}/{variation: \\w+}")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public DomainDocument getDocOfVariation(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id, @PathParam("variation") String variation) {
    @SuppressWarnings("unchecked")
    Class<? extends DomainDocument> type = (Class<? extends DomainDocument>) getDocType(entityType);
    DomainDocument doc = storageManager.getCompleteVariation(type, id, variation);
    if (doc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return doc;
  }

  private Class<? extends Document> getDocType(String entityType) {
    Class<? extends Document> type = docTypeRegistry.getTypeForIName(entityType);
    if (type == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return type;
  }

}
