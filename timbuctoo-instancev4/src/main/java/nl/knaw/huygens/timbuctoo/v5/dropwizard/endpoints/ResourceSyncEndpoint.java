package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;

@Path("v5/resourcesync")
public class ResourceSyncEndpoint {
  public static final String SOURCE_DESCRIPTION_PATH = "sourceDescription.xml";
  private final ResourceSync resourceSync;
  private final UriHelper uriHelper;

  public ResourceSyncEndpoint(ResourceSync resourceSync, UriHelper uriHelper) {
    this.resourceSync = resourceSync;
    this.uriHelper = uriHelper;
  }


  @GET
  @Path(SOURCE_DESCRIPTION_PATH)
  public Response getSourceDescription() throws FileNotFoundException {
    return streamFile(resourceSync.getSourceDescriptionFile(), MediaType.APPLICATION_XML_TYPE);
  }

  @GET
  @Path("{ownerId}/{dataSetName}/capabilityList.xml")
  public Response getCapabilityList(@PathParam("ownerId") String owner, @PathParam("dataSetName") String dataSetName)
    throws FileNotFoundException {
    return streamFile(resourceSync.getCapabilityListFile(owner, dataSetName), MediaType.APPLICATION_XML_TYPE);
  }

  @GET
  @Path("{ownerId}/{dataSetName}/description.xml")
  public Response getDescription(@PathParam("ownerId") String owner, @PathParam("dataSetName") String dataSetName)
    throws FileNotFoundException {
    return streamFile(resourceSync.getDataSetDescriptionFile(owner, dataSetName), MediaType.APPLICATION_XML_TYPE);
  }

  @GET
  @Path("{ownerId}/{dataSetName}/resourceList.xml")
  public Response getResourceList(@PathParam("ownerId") String owner, @PathParam("dataSetName") String dataSetName)
    throws FileNotFoundException {
    return streamFile(resourceSync.getResourceListFile(owner, dataSetName), MediaType.APPLICATION_XML_TYPE);
  }

  @GET
  @Path("{ownerId}/{dataSetName}/files/{fileId}")
  public Response getFile(@PathParam("ownerId") String owner,
                          @PathParam("dataSetName") String dataSet,
                          @PathParam("fileId") String fileId
  ) throws ResourceSyncException {
    CachedFile file = resourceSync.getFile(owner, dataSet, fileId);
    if (file == null || file.getFile() == null || !file.getFile().exists()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(file.getFile(), file.getMimeType()).build();
  }

  private Response streamFile(File file, MediaType mediaType) throws FileNotFoundException {
    if (!file.exists()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

    StreamingOutput output = output1 -> {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output1));

      for (String line; (line = bufferedReader.readLine()) != null; ) {
        writer.write(line.replace(ResourceSync.BASE_URI_PLACE_HOLDER, uriHelper.getBaseUri().toString()));
      }

      writer.flush();
    };

    return Response.ok(output, mediaType).build();
  }

}
