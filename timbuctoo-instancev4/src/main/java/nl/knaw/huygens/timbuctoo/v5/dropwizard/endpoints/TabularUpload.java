package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.bulkupload.TabularRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper.handleImportManagerResult;

@Path("/v5/{userId}/{dataSetId}/upload/table")
public class TabularUpload {

  private static final Logger LOG = LoggerFactory.getLogger(TabularUpload.class);
  private final AuthCheck authCheck;
  private final DataSetRepository dataSetRepository;
  private final ErrorResponseHelper errorResponseHelper;

  public TabularUpload(AuthCheck authCheck, DataSetRepository dataSetRepository,
                       ErrorResponseHelper errorResponseHelper) {
    this.authCheck = authCheck;
    this.dataSetRepository = dataSetRepository;
    this.errorResponseHelper = errorResponseHelper;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("file") final FormDataContentDisposition fileInfo,
                         @FormDataParam("fileMimeTypeOverride") final MediaType mimeTypeOverride,
                         FormDataMultiPart formData,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("userId") final String ownerId,
                         @PathParam("dataSetId") final String dataSetId,
                         @QueryParam("forceCreation") boolean forceCreation)
    throws DataStoreCreationException, FileStorageFailedException, ExecutionException, InterruptedException,
    LogStorageFailedException {

    final Either<Response, Response> result = authCheck.getOrCreate(authHeader, ownerId, dataSetId, forceCreation)
      .flatMap(userAndDs -> authCheck.hasAdminAccess(userAndDs.getLeft(), userAndDs.getRight()))
      .map(userAndDs -> {
        final MediaType mediaType = mimeTypeOverride == null ? body.getMediaType() : mimeTypeOverride;

        Optional<Loader> loader = LoaderFactory.createFor(mediaType.toString(), formData.getFields().entrySet().stream()
          .filter(entry -> entry.getValue().size() > 0)
          .filter(entry -> entry.getValue().get(0) != null)
          .filter(entry -> MediaTypes.typeEqual(MediaType.TEXT_PLAIN_TYPE, entry.getValue().get(0).getMediaType()))
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue())));

        if (!loader.isPresent()) {
          return errorResponseHelper.error(
            400,
            "We do not support the mediatype '" + mediaType + "'. Make sure to add the correct mediatype to the file " +
              "parameter. In curl you'd use `-F \"file=@<filename>;type=<mediatype>\"`. In a webbrowser you probably " +
              "have no way of setting the correct mimetype. So you can use a special parameter to override it: " +
              "`formData.append(\"fileMimeTypeOverride\", \"<mimetype>\");`"
          );
        }

        final DataSet dataSet = userAndDs.getRight();
        ImportManager importManager = dataSet.getImportManager();

        try {
          String fileToken = importManager.addFile(
            rdfInputStream,
            fileInfo.getName(),
            mediaType
          );
          Tuple<UUID, PlainRdfCreator> rdfCreator = dataSetRepository.registerRdfCreator(
            (statusConsumer) -> new TabularRdfCreator(
              loader.get(),
              statusConsumer,
              fileToken
            )
          );
          Future<ImportStatus> promise = importManager.generateLog(
            dataSet.getMetadata().getBaseUri(),
            dataSet.getMetadata().getBaseUri(),
            rdfCreator.getRight()
          );

          return handleImportManagerResult(promise);
        } catch (FileStorageFailedException | LogStorageFailedException e) {
          LOG.error("Tabular upload failed", e);
          return Response.serverError().build();
        }
      });
    if (result.isLeft()) {
      return result.getLeft();
    } else {
      return result.get();
    }
  }
}
