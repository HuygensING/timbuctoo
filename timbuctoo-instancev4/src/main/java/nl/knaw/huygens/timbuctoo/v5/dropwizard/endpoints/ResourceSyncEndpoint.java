package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("resourcesync")
public class ResourceSyncEndpoint {
  public static final String SOURCE_DESCRIPTION_PATH = "sourceDescription.xml";
  private final ResourceSync resourceSync;

  public ResourceSyncEndpoint(ResourceSync resourceSync) {
    this.resourceSync = resourceSync;
  }


  @GET
  @Path(SOURCE_DESCRIPTION_PATH)
  public Response getSourceDescription() {
    return Response.ok(resourceSync.getSourceDescriptionFile(), MediaType.APPLICATION_XML_TYPE).build();
  }

  @GET
  @Path("{ownerId}/{dataSetName}/capabilityList.xml")
  public Response getCapabilityList(@PathParam("ownerId") String owner, @PathParam("dataSetName") String dataSetName) {
    return Response.ok(resourceSync.getCapabilityListFile(owner, dataSetName), MediaType.APPLICATION_XML_TYPE).build();
  }

  @GET
  @Path("{ownerId}/{dataSetName}/resourceList.xml")
  public Response getResourceList(@PathParam("ownerId") String owner, @PathParam("dataSetName") String dataSetName) {
    return Response.ok(resourceSync.getResourceListFile(owner, dataSetName), MediaType.APPLICATION_XML_TYPE).build();
  }

  @GET
  @Path("{ownerId}/{dataSetName}/files/{fileId}")
  public Response getFile(@PathParam("ownerId") String owner,
                          @PathParam("dataSetName") String dataSet,
                          @PathParam("fileId") String fileId
  ) throws ResourceSyncException {
    CachedFile file = resourceSync.getFile(owner, dataSet, fileId);
    return Response.ok(file.getFile(), file.getMimeType().orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE)).build();
  }

}
