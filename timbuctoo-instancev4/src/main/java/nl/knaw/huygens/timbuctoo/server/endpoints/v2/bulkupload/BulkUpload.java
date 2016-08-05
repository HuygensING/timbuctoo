package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCreator;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Optional;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  public static final Logger LOG = LoggerFactory.getLogger(BulkUpload.class);
  private final BulkUploadService uploadService;
  private final UriHelper uriHelper;
  private final BulkUploadVre bulkUploadVre;
  private final LoggedInUserStore loggedInUserStore;
  private final VreAuthorizationCreator authorizationCreator;

  public BulkUpload(BulkUploadService uploadService, UriHelper uriHelper, BulkUploadVre bulkUploadVre,
                    LoggedInUserStore loggedInUserStore, VreAuthorizationCreator authorizationCreator) {
    this.uploadService = uploadService;
    this.uriHelper = uriHelper;
    this.bulkUploadVre = bulkUploadVre;
    this.loggedInUserStore = loggedInUserStore;
    this.authorizationCreator = authorizationCreator;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  //@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @Produces("text/html")
  public Response uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("file") InputStream fileInputStream,
    @HeaderParam("Authorization") String authorization) {

    if (vre == null || fileInputStream == null) {
      StringBuilder errorMessageBuilder = new StringBuilder("The following form params are missing:");
      errorMessageBuilder.append(System.getProperty("line.separator"));
      addIfNull(errorMessageBuilder, vre, "vre");
      addIfNull(errorMessageBuilder, fileInputStream, "file");

      return Response.status(Response.Status.BAD_REQUEST).entity(errorMessageBuilder.toString()).build();
    }

    Optional<User> user = loggedInUserStore.userFor(authorization);
    if (!user.isPresent()) {

      return Response.status(Response.Status.FORBIDDEN).entity("User not known").build();
    }

    try {
      authorizationCreator.createAuthorization(vre, user.get().getId(), UserRoles.ADMIN_ROLE);
    } catch (AuthorizationCreationException e) {
      LOG.error("Cannot add authorization for user {} and VRE {}", user.get().getId(), vre);
      LOG.error("Exception thrown", e);
      return Response.status(Response.Status.FORBIDDEN).entity("Unable to create authorization for user").build();
    }

    try {
      return Response.ok()
                     .entity(uploadService.saveToDb(vre, fileInputStream))
                     .location(bulkUploadVre.createUri(vre))
                     .build();
    } catch (AuthorizationUnavailableException | AuthorizationException | InvalidExcelFileException e) {
      e.printStackTrace();
      return Response.status(500).build();
    }
  }

  private void addIfNull(StringBuilder messageBuilder, Object parameter, String paramName) {
    if (parameter == null) {
      messageBuilder.append("\"");
      messageBuilder.append(paramName);
      messageBuilder.append("\"");
      messageBuilder.append(",");
    }
  }

}
