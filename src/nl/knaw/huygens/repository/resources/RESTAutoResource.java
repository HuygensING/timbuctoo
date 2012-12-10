package nl.knaw.huygens.repository.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.storage.StorageIterator;

import com.google.inject.Inject;

@Path("/resources/{resourceType: [a-zA-Z]+}")
public class RESTAutoResource {
    private final StorageManager storageManager;
    
    @Inject
    public RESTAutoResource(final StorageManager manager) {
      storageManager = manager;
    }
    
    @GET
    @Path("/")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    // FIXME use queryparam to delimit document search.
    public List<Document> getAllDocs(@PathParam("resourceType") String resourceType) {
      Class<? extends Document> cls = Document.getSubclassByString(resourceType);
      StorageIterator<? extends Document> allDocs = storageManager.getAll(cls);
      // FIXME Implementme
      //allDocs.
      return null;
    }
    
    @GET
    @Path("/{id: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
    public Document getDoc(@PathParam("resourceType") String resourceType, @PathParam("id") String id) {
      // FIXME
      return null;
    }

}
