package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCreator;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.io.ByteStreams.limit;
import static com.google.common.io.ByteStreams.toByteArray;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  public static final Logger LOG = LoggerFactory.getLogger(BulkUpload.class);
  private final BulkUploadService uploadService;
  private final UriHelper uriHelper;
  private final BulkUploadVre bulkUploadVre;
  private final LoggedInUserStore loggedInUserStore;
  private final VreAuthorizationCreator authorizationCreator;
  private final int maxCache;

  public BulkUpload(BulkUploadService uploadService, UriHelper uriHelper, BulkUploadVre bulkUploadVre,
                    LoggedInUserStore loggedInUserStore, VreAuthorizationCreator authorizationCreator, int maxCache) {
    this.uploadService = uploadService;
    this.uriHelper = uriHelper;
    this.bulkUploadVre = bulkUploadVre;
    this.loggedInUserStore = loggedInUserStore;
    this.authorizationCreator = authorizationCreator;
    this.maxCache = maxCache;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response uploadExcelFile(
    @FormDataParam("file") InputStream fileUpload,
    @FormDataParam("file") FormDataContentDisposition fileDetails,
    @FormDataParam("vreName") String vreName,
    @HeaderParam("Authorization") String authorization) {
    if (fileUpload == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("The file is missing").build();
    } else {
      Optional<User> user = loggedInUserStore.userFor(authorization);
      if (!user.isPresent()) {
        return Response.status(Response.Status.FORBIDDEN).entity("User not known").build();
      } else {
        final String unNamespacedVreName = vreName == null ? fileDetails.getFileName() : vreName;
        String namespacedVre = user.get().getPersistentId() + "_" + stripFunnyCharacters(unNamespacedVreName);
        try {
          authorizationCreator.createAuthorization(namespacedVre, user.get().getId(), UserRoles.ADMIN_ROLE);
        } catch (AuthorizationCreationException e) {
          LOG.error("Cannot add authorization for user {} and VRE {}", user.get().getId(), namespacedVre);
          LOG.error("Exception thrown", e);
          return Response.status(Response.Status.FORBIDDEN).entity("Unable to create authorization for user").build();
        }

        byte[] bytes;
        try {
          bytes = toByteArray(limit(fileUpload, maxCache));
          if (fileUpload.read() != -1) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("The file may not be larger then " + humanReadableByteCount(maxCache) + " bytes")
                           .build();
          }

        } catch (IOException e) {
          LOG.error("Reading upload failed", e);
          return Response.status(400).build();
        }
        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
        final AtomicInteger writeErrors = new AtomicInteger(0);
        new Thread() {
          public void run() {
            try {
              uploadService.saveToDb(namespacedVre, bytes, fileDetails.getFileName(), unNamespacedVreName, msg -> {
                try {
                  //write json objects
                  if (writeErrors.get() < 5) {
                    output.write(msg + "\n");
                  }
                } catch (IOException e) {
                  LOG.error("Could not write to output stream", e);
                  writeErrors.incrementAndGet();
                }
              });
            } catch (InvalidFileException | IOException e) {
              LOG.error("Something went wrong while importing a file", e);
              try {
                if (writeErrors.get() < 5) {
                  output.write("failure: " + e.getMessage());
                }
              } catch (IOException outputError) {
                LOG.error("Couldn't write to output stream", outputError);
              }
            } finally {
              try {
                output.close();
              } catch (IOException e) {
                LOG.error("Couldn't close the output stream", e);
              }
            }
          }
        }.start();
        // the output will be probably returned even before
        // a first chunk is written by the new thread
        return Response.ok()
          .entity(output)
          .location(bulkUploadVre.createUri(namespacedVre))
          .build();
      }
    }
  }


  private String stripFunnyCharacters(String vre) {
    return vre.replaceFirst("\\.[a-zA-Z]+$", "").replaceAll("[^a-zA-Z-]", "_");
  }

  private String humanReadableByteCount(long bytes) {
    int unit = 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = ("KMGTPE").charAt(exp - 1) + "iB";
    return String.format("%.2f %s", bytes / Math.pow(unit, exp), pre);
  }
}
