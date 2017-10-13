package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;

@Path("v5/openrefinereconciliation")
public class OpenRefineReconciliationEndpoint {

  private ResourceSync resourceSync;
  private UriHelper uriHelper;

  public OpenRefineReconciliationEndpoint(ResourceSync resourceSync, UriHelper uriHelper) {
    this.resourceSync = resourceSync;
    this.uriHelper = uriHelper;
  }

  @GET
  @Path("getName{firstName}/{lastName}")
  public Response getname(@PathParam("firstName") String firstName,
                          @PathParam("lastName") String lastName
  ) throws ResourceSyncException {

    String result = "";
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }
}

