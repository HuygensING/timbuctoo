package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.datastores.rssource.RsDocumentBuilder;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Optional;

@Path("v5/rs")
public class RsEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(RsEndpoint.class);
  private final RsDocumentBuilder rsDocumentBuilder;
  private final UserValidator userValidator;

  public RsEndpoint(RsDocumentBuilder rsDocumentBuilder, UserValidator userValidator) {
    this.rsDocumentBuilder = rsDocumentBuilder;
    this.userValidator = userValidator;
  }

  @GET
  @Path(RsDocumentBuilder.SOURCE_DESCRIPTION_PATH)
  @Produces(MediaType.APPLICATION_XML)
  public Response getSourceDescription(@HeaderParam("authorization") String authHeader) {
    return Response.ok(rsDocumentBuilder.getSourceDescription(getUser(authHeader))).build();
  }

  @GET
  @Path("{ownerId}/{dataSetName}/capabilitylist.xml")
  @Produces(MediaType.APPLICATION_XML)
  public Response getCapabilityList(@HeaderParam("authorization") String authHeader,
                                    @PathParam("ownerId") String owner,
                                    @PathParam("dataSetName") String dataSetName) {
    return Response.ok(rsDocumentBuilder.getCapabilityList(getUser(authHeader), owner, dataSetName)).build();
  }

  @GET
  @Path("{ownerId}/{dataSetName}/resourcelist.xml")
  @Produces(MediaType.APPLICATION_XML)
  public Response getResourceList(@HeaderParam("authorization") String authHeader,
                                  @PathParam("ownerId") String owner,
                                  @PathParam("dataSetName") String dataSetName) throws FileNotFoundException {
    Optional<File> maybeResourceList = rsDocumentBuilder.getResourceListFile(getUser(authHeader), owner, dataSetName);
    if (maybeResourceList.isPresent()) {
      return streamFile(maybeResourceList.get(), MediaType.APPLICATION_XML_TYPE);
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("{ownerId}/{dataSetName}/files/{fileId}")
  public Response getFile(@HeaderParam("authorization") String authHeader,
                          @PathParam("ownerId") String owner,
                          @PathParam("dataSetName") String dataSetName,
                          @PathParam("fileId") String fileId
  ) throws ResourceSyncException {
    User user = getUser(authHeader);
    Optional<CachedFile> maybeFile = rsDocumentBuilder.getCachedFile(user, owner, dataSetName, fileId);
    if (maybeFile.isPresent()) {
      CachedFile cachedFile = maybeFile.get();
      File file = cachedFile.getFile();
      if (file != null && file.exists()) {
        return Response.ok(cachedFile.getFile(), cachedFile.getMimeType()).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } else if (user == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  private User getUser(String authHeader) {
    User user = null;
    try {
      user = userValidator.getUserFromAccessToken(authHeader).orElse(null);
    } catch (UserValidationException e) {
      LOG.error("Exception validating user", e);
    }
    return user;
  }

  // Can be dismissed on dynamic creation of resourcelist
  private Response streamFile(File file, MediaType mediaType) throws FileNotFoundException {
    if (!file.exists()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

    StreamingOutput output = output1 -> {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output1));

      for (String line; (line = bufferedReader.readLine()) != null; ) {
        writer.write(
          line.replace(ResourceSync.BASE_URI_PLACE_HOLDER, rsDocumentBuilder.getUriHelper().getBaseUri().toString()));
      }

      writer.flush();
    };

    return Response.ok(output, mediaType).build();
  }

}
