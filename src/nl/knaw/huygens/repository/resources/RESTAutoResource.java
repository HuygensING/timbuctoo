package nl.knaw.huygens.repository.resources;

import java.io.IOException;
import java.util.List;

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

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.generic.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("resources/{entityType: [a-zA-Z]+}")
public class RESTAutoResource {

  private final StorageManager storageManager;
  private final DocumentTypeRegister docTypeRegistry;

  @Inject
  public RESTAutoResource(final StorageManager manager, final DocumentTypeRegister docTypeRegistry) {
    storageManager = manager;
    this.docTypeRegistry = docTypeRegistry;
  }

  @GET
  @Path("/all")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends Document> getAllDocs(@PathParam("entityType") String entityType, @QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(404);
    }
    List<? extends Document> allLimited = storageManager.getAllLimited(cls, start, rows);
    return allLimited;
  }

  // TODO: test me
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/all")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void getAllDocs(@PathParam("entityType") String entityType, Document input) throws IOException {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(404);
    }
    Class<T> typedCls;
    T typedDoc;
    try {
      @SuppressWarnings("unchecked")
      Class<T> myTypedCls = (Class<T>) cls;
      typedCls = myTypedCls;
      @SuppressWarnings("unchecked")
      T myTypedDoc = (T) input;
      typedDoc = myTypedDoc;
    } catch (ClassCastException ex) {
      throw new WebApplicationException(404);
    }
    storageManager.addDocument(typedDoc, typedCls);
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public Document getDoc(@PathParam("entityType") String entityType, @PathParam("id") String id) {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(404);
    }
    Document doc = storageManager.getCompleteDocument(id, cls);
    if (doc == null) {
      throw new WebApplicationException(404);
    }
    return doc;
  }

  // TODO: test this! :-)
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam("entityType") String entityType, @PathParam("id") String id, Document input) throws IOException {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(404);
    }
    Class<T> typedCls;
    T typedDoc;
    try {
      @SuppressWarnings("unchecked")
      Class<T> myTypedCls = (Class<T>) cls;
      typedCls = myTypedCls;
      @SuppressWarnings("unchecked")
      T myTypedDoc = (T) input;
      typedDoc = myTypedDoc;
    } catch (ClassCastException ex) {
      throw new WebApplicationException(404);
    }
    storageManager.modifyDocument(typedDoc, typedCls);
  }

  // TODO: test this! :-)
  @DELETE
  @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
  @JsonView(JsonViews.WebView.class)
  public <T extends Document> void putDoc(@PathParam("entityType") String entityType, @PathParam("id") String id) throws IOException {
    Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(404);
    }
    Class<T> typedCls;
    T typedDoc;
    try {
      @SuppressWarnings("unchecked")
      Class<T> myTypedCls = (Class<T>) cls;
      typedCls = myTypedCls;
      typedDoc = storageManager.getDocument(id, typedCls);
    } catch (ClassCastException ex) {
      throw new WebApplicationException(404);
    }
    storageManager.removeDocument(typedDoc, typedCls);
  }
}
