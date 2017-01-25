package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.UserRoles;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.vre.Vre.PublishState.MAPPING_EXECUTION;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.PublishState.UPLOADING;
import static org.apache.poi.util.IOUtils.copy;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  public static final Logger LOG = LoggerFactory.getLogger(BulkUpload.class);
  private final BulkUploadService uploadService;
  private final BulkUploadVre bulkUploadVre;
  private final LoggedInUsers loggedInUsers;
  private final VreAuthorizationCrud authorizationCreator;
  private final int maxCache;
  private final UserPermissionChecker permissionChecker;
  private final TransactionEnforcer transactionEnforcer;

  public BulkUpload(BulkUploadService uploadService, BulkUploadVre bulkUploadVre,
                    LoggedInUsers loggedInUsers, VreAuthorizationCrud authorizationCreator, int maxCache,
                    UserPermissionChecker permissionChecker, TransactionEnforcer transactionEnforcer) {
    this.uploadService = uploadService;
    this.bulkUploadVre = bulkUploadVre;
    this.loggedInUsers = loggedInUsers;
    this.authorizationCreator = authorizationCreator;
    this.maxCache = maxCache;
    this.permissionChecker = permissionChecker;
    this.transactionEnforcer = transactionEnforcer;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response upload(@HeaderParam("Authorization") String authorization,
                         @FormDataParam("vreName") String vreName,
                         FormDataMultiPart parts) {
    Optional<User> user = loggedInUsers.userFor(authorization);
    if (!user.isPresent()) {
      return Response.status(Response.Status.FORBIDDEN).entity("User not known").build();
    }

    if (vreName == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("vreName missing").build();
    }
    String namespacedVre = user.get().getPersistentId() + "_" + stripFunnyCharacters(vreName);

    try {
      authorizationCreator.createAuthorization(namespacedVre, user.get().getId(), UserRoles.ADMIN_ROLE);
    } catch (AuthorizationCreationException e) {
      LOG.error("Cannot add authorization for user {} and VRE {}", user.get().getId(), namespacedVre);
      LOG.error("Exception thrown", e);
      return Response.status(Response.Status.FORBIDDEN).entity("Unable to create authorization for user").build();
    }

    List<FormDataBodyPart> files = parts.getFields("file");
    if (files == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("files missing").build();
    }

    ChunkedOutput<String> output;
    try {
      output = executeUpload(files, vreName, namespacedVre);
    } catch (IOException e) {
      LOG.error("Reading upload failed", e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }

    return Response.ok()
                   .entity(output)
                   .location(bulkUploadVre.createUri(namespacedVre))
                   .build();
  }

  @PUT
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response reUploadExcelFile(@FormDataParam("vreId") String vreName,
                                    @HeaderParam("Authorization") String authorization,
                                    FormDataMultiPart parts) {

    // First check permission
    final Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorization);
    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }
    return transactionEnforcer.executeAndReturn(timbuctooActions -> {

      // Try to find the vre
      final Vre vre = timbuctooActions.getVre(vreName);
      if (vre == null) {
        // not found
        return TransactionStateAndResult.commitAndReturn(Response.status(Response.Status.NOT_FOUND).build());
      }

      // Check whether vre is currently in a transitional state
      if (vre.getPublishState() == UPLOADING || vre.getPublishState() == MAPPING_EXECUTION) {
        // Data from this vre is currently being transformed, so re-upload is dangerous
        return TransactionStateAndResult
          .commitAndReturn(Response.status(Response.Status.PRECONDITION_FAILED).build());
      }

      List<FormDataBodyPart> files = parts.getFields("file");
      if (files == null) {
        return TransactionStateAndResult
          .commitAndReturn(
            Response.status(Response.Status.BAD_REQUEST).entity("files missing").build());
      }

      try {
        ChunkedOutput<String> output = executeUpload(files, vre.getMetadata().getLabel(), vreName);
        return TransactionStateAndResult.commitAndReturn(
          Response.ok()
                  .location(bulkUploadVre.createUri(vreName))
                  .entity(output)
                  .build());
      } catch (IOException e) {
        return TransactionStateAndResult.commitAndReturn(
          Response.status(Response.Status.BAD_REQUEST)
                  .entity(e.getMessage())
                  .build()
        );
      } catch (IllegalArgumentException e) {
        return TransactionStateAndResult.commitAndReturn(
          Response.status(Response.Status.BAD_REQUEST)
                  .entity(e.getMessage())
                  .build()
        );
      }
    });
  }

  private ChunkedOutput<String> executeUpload(List<FormDataBodyPart> parts, String vreLabel, String vreName)
    throws IOException {
    ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

    //Store the files on the filesystem.
    // - Parsing them without buffering is usually not possible
    // - Storing them in memory is more expensive then saving them on the FS
    List<Tuple<String, File>> tempFiles = new ArrayList<>();
    //Limit the total size of all the files to maxCache
    long sizeLeft = maxCache;
    for (FormDataBodyPart part : parts) {
      FormDataContentDisposition fileDetails = part.getFormDataContentDisposition();
      InputStream fileUpload = part.getValueAs(InputStream.class);
      File tempFile = File.createTempFile("timbuctoo-bulkupload-", null, null);
      LimitOutputStream fos = null;
      try {
        fos = new LimitOutputStream(new FileOutputStream(tempFile), sizeLeft);
        copy(fileUpload, fos);
      } finally {
        if (fos != null) {
          sizeLeft = fos.getLeft();
          fos.close();
        }
      }
      tempFiles.add(Tuple.tuple(fileDetails.getFileName(), tempFile));
    }

    new Thread() {
      public void run() {
        final int[] writeErrors = {0};
        try {
          uploadService.saveToDb(vreName, tempFiles, vreLabel, msg -> {
            writeMessage(writeErrors, msg);
          });
        } catch (InvalidFileException | IOException e) {
          LOG.error("Something went wrong while importing a file", e);
          writeMessage(writeErrors, "failure: The file could not be read");
        } catch (Exception e) {
          LOG.error("An unexpected exception occurred", e);
          writeMessage(writeErrors, "failure: The file could not be read");
        } finally {
          try {
            output.close();
            tempFiles.forEach(f -> {
              if (!f.getRight().delete()) {
                LOG.error("couldn't delete " + f.getRight().getAbsolutePath());
              }
            });
          } catch (IOException e) {
            LOG.error("Couldn't close the output stream", e);
          } catch (Exception e) {
            LOG.error("An unexpected exception occurred", e);
          }
        }
      }

      private void writeMessage(int[] writeErrors, String msg) {
        try {
          //write json objects
          if (writeErrors[0] < 5) {
            output.write(msg + "\n");
          }
        } catch (IOException e) {
          LOG.error("Could not write to output stream", e);
          writeErrors[0]++;
        }
      }
    }.start();

    return output;
  }

  private String stripFunnyCharacters(String vre) {
    return vre.replaceFirst("\\.[a-zA-Z]+$", "").replaceAll("[^a-zA-Z-]", "_");
  }


  class LimitOutputStream extends FilterOutputStream {

    private long limit;

    public LimitOutputStream(OutputStream out, long limit) {
      super(out);
      this.limit = limit;
    }

    public long getLeft() {
      return limit;
    }

    public void write(@Nonnull byte... bytes) throws IOException {
      long left = Math.min(bytes.length, limit);
      if (left <= 0) {
        return;
      }
      limit -= left;
      out.write(bytes, 0, (int)left);
    }

    public void write(int byt) throws IOException {
      if (limit <= 0) {
        return;
      }
      limit--;
      out.write(byt);
    }

    public void write(@Nonnull byte[] bytes, int off, int len) throws IOException {
      long left = Math.min(len,limit);
      if (left <= 0) {
        return;
      }
      limit -= left;
      out.write(bytes, off, (int) left);
    }
  }
}
