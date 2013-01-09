package nl.knaw.huygens.repository.resources;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.generic.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("resources/{resourceType: [a-zA-Z]+}")
public class RESTAutoResource {
    private final StorageManager storageManager;
    
    @Inject
    public RESTAutoResource(final StorageManager manager) {
      storageManager = manager;
    }
    
    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    @JsonView(JsonViews.WebView.class)
    public List<? extends Document> getAllDocs(@PathParam("resourceType") String resourceType,
        @QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
      Class<? extends Document> cls = Document.getSubclassByString(resourceType);
      if (cls == null) {
        throw new WebApplicationException(404);
      }
      List<? extends Document> allLimited = storageManager.getAllLimited(cls, start, rows);
      return allLimited;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
    @JsonView(JsonViews.WebView.class)
    public Document getDoc(@PathParam("resourceType") String resourceType, @PathParam("id") String id) {
      Class<? extends Document> cls = Document.getSubclassByString(resourceType);
      if (cls == null) {
        throw new WebApplicationException(404);
      }
      Document doc = storageManager.getCompleteDocument(id, cls);
      if (doc == null) {
        throw new WebApplicationException(404);
      }
      return doc;
    }

}
