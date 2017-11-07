package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.access.MdbLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv.CsvLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.server.BulkUploadService;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.status;
import static nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult.commitAndReturn;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.PublishState.MAPPING_EXECUTION;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.PublishState.UPLOADING;
import static org.apache.commons.io.IOUtils.copy;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  public static final Logger LOG = LoggerFactory.getLogger(BulkUpload.class);
  private final BulkUploadService uploadService;
  private final BulkUploadVre bulkUploadVre;
  private final UserValidator userValidator;
  private final VreAuthorizationCrud authorizationCreator;
  private final int maxCache;
  private final UserPermissionChecker permissionChecker;
  private final TransactionEnforcer transactionEnforcer;
  private final int maxFiles;
  private final VreAuthIniter vreAuthIniter;

  public BulkUpload(BulkUploadService uploadService, BulkUploadVre bulkUploadVre,
                    UserValidator userValidator, VreAuthorizationCrud authorizationCreator, int maxCache,
                    UserPermissionChecker permissionChecker, TransactionEnforcer transactionEnforcer, int maxFiles) {
    this.uploadService = uploadService;
    this.bulkUploadVre = bulkUploadVre;
    this.userValidator = userValidator;
    this.authorizationCreator = authorizationCreator;
    this.maxCache = maxCache;
    this.permissionChecker = permissionChecker;
    this.transactionEnforcer = transactionEnforcer;
    this.maxFiles = maxFiles;
    this.vreAuthIniter = new VreAuthIniter(userValidator, transactionEnforcer, authorizationCreator);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response upload(@HeaderParam("Authorization") String authorization,
                         @FormDataParam("vreName") String vreName,
                         @FormDataParam("uploadType") String uploadType,
                         FormDataMultiPart parts) {
    Map<String, String> formData = parts.getFields().entrySet().stream()
      .filter(entry -> !entry.getKey().equals("file"))
      .filter(entry -> !entry.getKey().equals("vreId"))
      .filter(entry -> !entry.getKey().equals("uploadType"))
      .filter(entry -> entry.getValue().size() > 0 && entry.getValue().get(0) != null)
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue()));

    return vreAuthIniter
      .addVreAuthorizations(authorization, vreName)
      .getOrElseGet(namespacedVre -> {
        List<FormDataBodyPart> files = parts.getFields("file");
        if (files == null) {
          return status(Response.Status.BAD_REQUEST).entity("files missing").build();
        }

        try {
          return executeUpload(files, formData, uploadType, vreName, namespacedVre);
        } catch (IOException e) {
          LOG.error("Reading upload failed", e);
          return status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .build();
        } catch (IllegalArgumentException e) {
          return status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .build();
        }
      });
  }

  @PUT
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response reUploadExcelFile(@FormDataParam("vreId") String vreName,
                                    @HeaderParam("Authorization") String authorization,
                                    @FormDataParam("uploadType") String uploadType,
                                    FormDataMultiPart parts) {

    Map<String, String> formData = parts.getFields().entrySet().stream()
      .filter(entry -> !entry.getKey().equals("file"))
      .filter(entry -> !entry.getKey().equals("vreId"))
      .filter(entry -> !entry.getKey().equals("uploadType"))
      .filter(entry -> entry.getValue().size() > 0 && entry.getValue().get(0) != null)
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue()));
    // First check permission
    final Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorization);
    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    Vre vre = transactionEnforcer.executeAndReturn(
      timbuctooActions -> commitAndReturn(timbuctooActions.getVre(vreName))
    );

    if (vre == null) {
      // not found
      return status(Response.Status.NOT_FOUND).build();
    }

    // Check whether vre is currently in a transitional state
    if (vre.getPublishState() == UPLOADING || vre.getPublishState() == MAPPING_EXECUTION) {
      // Data from this vre is currently being transformed, so re-upload is dangerous
      return status(Response.Status.PRECONDITION_FAILED).build();
    }

    List<FormDataBodyPart> files = parts.getFields("file");
    if (files == null) {
      return status(Response.Status.BAD_REQUEST).entity("files missing").build();
    }

    try {
      return executeUpload(files, formData, uploadType, vre.getMetadata().getLabel(), vreName);
    } catch (IOException | IllegalArgumentException e) {
      return status(Response.Status.BAD_REQUEST)
        .entity(e.getMessage())
        .build();
    }
  }

  private Response executeUpload(List<FormDataBodyPart> parts, Map<String, String> form, String uploadType,
                                 String vreLabel, String vreName)
    throws IOException {

    ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

    Loader loader;
    if (uploadType == null || uploadType.equals("xlsx")) {
      loader = new AllSheetLoader();
    } else if (uploadType.equals("csv")) {
      try {
        loader = new CsvLoader(form);
      } catch (IllegalArgumentException e) {
        return status(Response.Status.BAD_REQUEST)
          .entity(e.toString())
          .build();
      }
    } else if (uploadType.equals("dataperfect")) {
      loader = new DataPerfectLoader();
    } else if (uploadType.equals("mdb")) {
      loader = new MdbLoader();
    } else {
      return status(Response.Status.BAD_REQUEST)
        .entity("failure: unknown uploadType" + uploadType)
        .build();
    }

    //Store the files on the filesystem.
    // - Parsing them without buffering is usually not possible
    // - Storing them in memory is more expensive then saving them on the FS
    List<Tuple<String, File>> tempFiles = new ArrayList<>();
    //Limit the total size of all the files to maxCache
    long sizeLeft = maxCache;
    int fileCount = 0;
    for (FormDataBodyPart part : parts) {
      if (fileCount++ == maxFiles) {
        break;
      }
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
          uploadService.saveToDb(vreName, loader, tempFiles, vreLabel, msg -> {
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

    return Response.ok()
      .location(bulkUploadVre.createUri(vreName))
      .entity(output)
      .build();
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

    public void write(@Nonnull byte[] bytes) throws IOException {
      long left = Math.min(bytes.length, limit);
      if (left <= 0) {
        return;
      }
      limit -= left;
      out.write(bytes, 0, (int) left);
    }

    public void write(int byt) throws IOException {
      if (limit <= 0) {
        return;
      }
      limit--;
      out.write(byt);
    }

    public void write(@Nonnull byte[] bytes, int off, int len) throws IOException {
      long left = Math.min(len, limit);
      if (left <= 0) {
        return;
      }
      limit -= left;
      out.write(bytes, off, (int) left);
    }
  }
}
