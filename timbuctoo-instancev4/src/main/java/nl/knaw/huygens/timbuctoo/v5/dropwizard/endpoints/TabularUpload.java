package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.bulkupload.TabularRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.message.internal.MediaTypes;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;

@Path("/v5/{userId}/{dataSetId}/upload/table")
public class TabularUpload {

  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final DataSetRepository dataSetRepository;
  private final ErrorResponseHelper errorResponseHelper;

  public TabularUpload(LoggedInUsers loggedInUsers, Authorizer authorizer, DataSetRepository dataSetRepository,
                       TimbuctooRdfIdHelper rdfIdHelper, ErrorResponseHelper errorResponseHelper) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.dataSetRepository = dataSetRepository;
    this.rdfIdHelper = rdfIdHelper;
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

    final Response response = checkWriteAccess(
      dataSetRepository::dataSetExists, authorizer, loggedInUsers, authHeader, ownerId, dataSetId
    );
    if (response != null) {
      return response;
    }

    final MediaType mediaType = mimeTypeOverride == null ? body.getMediaType() : mimeTypeOverride;

    Optional<Loader> loader = LoaderFactory.createFor(mediaType.toString(), formData.getFields().entrySet().stream()
      .filter(entry -> entry.getValue().size() > 0)
      .filter(entry -> entry.getValue().get(0) != null)
      .filter(entry -> MediaTypes.typeEqual(MediaType.TEXT_PLAIN_TYPE, entry.getValue().get(0).getMediaType()))
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue())));

    if (mediaType == null || !loader.isPresent()) {
      return Response.status(400)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"error\": \"We do not support the mediatype '" + mediaType + "'. Make sure to add the correct " +
          "mediatype to the file parameter. In curl you'd use `-F \"file=@<filename>;type=<mediatype>\"`. In a " +
          "webbrowser you probably have no way of setting the correct mimetype. So you can use a special parameter " +
          "to override it: `formData.append(\"fileMimeTypeOverride\", \"<mimetype>\");`\"}")
        .build();
    }

    final Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(ownerId, dataSetId);
    final DataSet dataSet;
    if (dataSetOpt.isPresent()) {
      dataSet = dataSetOpt.get();
    } else if (forceCreation) {
      dataSet = dataSetRepository.createDataSet(ownerId, dataSetId);
    } else {
      return errorResponseHelper.dataSetNotFound(ownerId, dataSetId);
    }

    ImportManager importManager = dataSet.getImportManager();

    String fileToken = importManager.addFile(
      rdfInputStream,
      fileInfo.getName(),
      mediaType
    );


    Tuple<UUID, PlainRdfCreator> rdfCreator = dataSetRepository.registerRdfCreator(
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
