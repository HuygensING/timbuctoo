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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.generic.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("resources/{entityType: [a-zA-Z]+}")
public class RESTAutoResource {

  private static final String ID_PARAM = "id";

  public static final String ENTITY_PARAM = "entityType";

  private final StorageManager storageManager;
  private final DocumentTypeRegister docTypeRegistry;

  @Inject
  public RESTAutoResource(final StorageManager manager, final DocumentTypeRegister registry) {
    storageManager = manager;
    docTypeRegistry = registry;
  }

  @GET
  @Path("/all")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public List<? extends Document> getAllDocs(@PathParam(ENTITY_PARAM) String entityType, @QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    Class<? extends Document> type = getDocType(entityType);
    return storageManager.getAllLimited(type, start, rows);
  }

  // TODO: test me
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/all")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void getAllDocs(@PathParam(ENTITY_PARAM) String entityType, Document input) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> type = (Class<T>) getDocType(entityType);
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.addDocument(type, typedDoc);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  @RolesAllowed("USER")
  public Document getDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id) {
    Class<? extends Document> type = getDocType(entityType);
    Document doc = storageManager.getCompleteDocument(type, id);
    if (doc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return doc;
  }

  // TODO: test this! :-)
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id, Document input) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> type = (Class<T>) getDocType(entityType);
      @SuppressWarnings("unchecked")
      T typedDoc = (T) input;
      storageManager.modifyDocument(type, typedDoc);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  // TODO: test this! :-)
  @DELETE
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id) throws IOException {
    try {
      @SuppressWarnings("unchecked")
      Class<T> type = (Class<T>) getDocType(entityType);
      T typedDoc = storageManager.getDocument(type, id);
      storageManager.removeDocument(type, typedDoc);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @JsonView(JsonViews.WebView.class)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}/{variation: \\w+}")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public Document getDocWithOfVariation(@PathParam(ENTITY_PARAM) String entityType, @PathParam(ID_PARAM) String id, @PathParam("variation") String variation) {
    Class<? extends Document> type = getDocType(entityType);
    Document doc = storageManager.getCompleteDocument(type, id, variation);
    if (doc == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return doc;
  }

  private Class<? extends Document> getDocType(String entityType) {
    Class<? extends Document> type = docTypeRegistry.getClassFromTypeString(entityType);
    if (type == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return type;
  }

}
