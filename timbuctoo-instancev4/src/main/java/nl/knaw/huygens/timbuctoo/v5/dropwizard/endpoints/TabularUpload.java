package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.TabularRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;

@Path("/v5/{userId}/{dataSetId}/upload/table")
public class TabularUpload {

  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final DataSetRepository dataSetRepository;

  public TabularUpload(LoggedInUsers loggedInUsers, Authorizer authorizer, DataSetRepository dataSetRepository,
                       TimbuctooRdfIdHelper rdfIdHelper) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.dataSetRepository = dataSetRepository;
    this.rdfIdHelper = rdfIdHelper;
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
                         @PathParam("dataSetId") final String dataSetId)
    throws DataStoreCreationException, FileStorageFailedException, ExecutionException, InterruptedException,
    LogStorageFailedException {

    final Response response = checkWriteAccess(
      dataSetRepository::dataSetExists, authorizer, loggedInUsers, authHeader, ownerId, dataSetId
    );
    if (response != null) {
      return response;
    }

    final MediaType mediaType = mimeTypeOverride == null ? body.getMediaType() : mimeTypeOverride;

    Optional<Loader> loader = LoaderFactory.createFor(mediaType, formData);

    if (mediaType == null || !loader.isPresent()) {
      return Response.status(400)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"error\": \"We do not support the mediatype '" + mediaType + "'. Make sure to add the correct " +
          "mediatype to the file parameter. In curl you'd use `-F \"file=@<filename>;type=<mediatype>\"`. In a " +
          "webbrowser you probably have no way of setting the correct mimetype. So you can use a special parameter " +
          "to override it: `formData.append(\"fileMimeTypeOverride\", \"<mimetype>\");`\"}")
        .build();
    }

    ImportManager importManager = dataSetRepository.createDataSet(ownerId, dataSetId).getImportManager();

    String fileToken = importManager.addFile(
      rdfInputStream,
      fileInfo.getName(),
      mediaType
    );


    Tuple<UUID, RdfCreator> rdfCreator = dataSetRepository.registerRdfCreator(
      (statusConsumer) -> new TabularRdfCreator(
        importManager,
        loader.get(),
        ownerId,
        dataSetId,
        statusConsumer,
        fileToken,
        rdfIdHelper
      )
    );

    Future<?> promise = importManager.generateLog(
      rdfIdHelper.dataSet(ownerId, dataSetId),
      rdfIdHelper.dataSet(ownerId, dataSetId),
      rdfCreator.getRight()
    );

    promise.get(); // Wait until the import is done.

    return Response.noContent().build();

    // return Response.created(fromResource(TabularUpload.class)
    //   .path(rdfCreator.getLeft().toString())
    //   .buildFromMap(ImmutableMap.of("userId", ownerId, "dataSetId", dataSetId))
    // ).build();
  }

  @GET
  @Path("{importId}")
  public Response getStatus(@PathParam("importId") final UUIDParam importId) {
    Optional<String> status = dataSetRepository.getStatus(importId.get());

    if (status.isPresent()) {
      return Response.ok(status).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

}
