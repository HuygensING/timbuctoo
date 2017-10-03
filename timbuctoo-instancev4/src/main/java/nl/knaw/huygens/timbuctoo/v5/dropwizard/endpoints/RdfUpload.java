package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;

@Path("/v5/{userId}/{dataSet}/upload/rdf")
public class RdfUpload {

  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final DataSetRepository dataSetRepository;
  private final ErrorResponseHelper errorResponseHelper;


  public RdfUpload(LoggedInUsers loggedInUsers, Authorizer authorizer, DataSetRepository dataSetRepository,
                   ErrorResponseHelper errorResponseHelper) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.dataSetRepository = dataSetRepository;
    this.errorResponseHelper = errorResponseHelper;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("fileMimeTypeOverride") final MediaType mimeTypeOverride,
                         @FormDataParam("encoding") final String encoding,
                         @FormDataParam("baseUri") final URI baseUri,
                         @FormDataParam("defaultGraph") final URI defaultGraph,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("userId") final String userId,
                         @PathParam("dataSet") final String dataSetId,
                         @QueryParam("forceCreation") boolean forceCreation,
                         @QueryParam("async") final boolean async)
    throws ExecutionException, InterruptedException, LogStorageFailedException, DataStoreCreationException {

    final Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(userId, dataSetId);
    final Response response = checkWriteAccess(
      userId,
      dataSetId,
      (ownerId, dataSetId1) -> dataSetRepository.getDataSet(ownerId, dataSetId1).map(DataSet::getMetadata),
      authorizer,
      loggedInUsers,
      authHeader
    );
    if (response != null) {
      return response;
    }

    final MediaType mediaType = mimeTypeOverride == null ? body.getMediaType() : mimeTypeOverride;

    final DataSet dataSet;
    if (dataSetOpt.isPresent()) {
      dataSet = dataSetOpt.get();
    } else if (forceCreation) {
      dataSet = dataSetRepository.createDataSet(userId, dataSetId);
    } else {
      return errorResponseHelper.dataSetNotFound(userId, dataSetId);
    }

    ImportManager importManager = dataSet.getImportManager();

    if (mediaType == null || !importManager.isRdfTypeSupported(mediaType)) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"error\": \"We do not support the mediatype '" + mediaType + "'. Make sure to add the correct " +
          "mediatype to the file parameter. In curl you'd use `-F \"file=@<filename>;type=<mediatype>\"`. In a " +
          "webbrowser you probably have no way of setting the correct mimetype. So you can use a special parameter " +
          "to override it: `formData.append(\"fileMimeTypeOverride\", \"<mimetype>\");`\"}")
        .build();
    }

    Future<?> promise = importManager.addLog(
      baseUri == null ? dataSet.getMetadata().getBaseUri() : baseUri.toString(),
      defaultGraph == null ? dataSet.getMetadata().getBaseUri() : defaultGraph.toString(),
      body.getContentDisposition().getFileName(),
      rdfInputStream,
      Optional.of(Charset.forName(encoding)),
      mediaType
    );
    if (!async) {
      promise.get();
    }

    return Response.noContent().build();
  }

}
