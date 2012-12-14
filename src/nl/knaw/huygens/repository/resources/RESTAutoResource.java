package nl.knaw.huygens.repository.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.storage.JsonViews;

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
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @JsonView(JsonViews.WebView.class)
    public <T> Response getAllDocs(@PathParam("resourceType") String resourceType, @QueryParam("rows") int rows, @QueryParam("start") int start) {
      Class<? extends Document> cls = Document.getSubclassByString(resourceType);
      List<? extends Document> allLimited = storageManager.getAllLimited(cls, start, rows);
      System.err.println("Test");
      return Response.ok(getWrappedEntity(allLimited)).build();
    }
    
    private <T extends Document> GenericEntity<List<T>> getWrappedEntity(List<T> allLimited) {
      return new GenericEntity<List<T>>(allLimited) {};
    }

    @GET
    @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
    public Document getDoc(@PathParam("resourceType") String resourceType, @PathParam("id") String id) {
      Class<? extends Document> cls = Document.getSubclassByString(resourceType);
      return storageManager.getCompleteDocument(id, cls);
    }

}
