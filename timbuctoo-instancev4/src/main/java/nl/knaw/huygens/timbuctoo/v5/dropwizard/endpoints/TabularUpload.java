package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory.LoaderConfig;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.TabularRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
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
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static javax.ws.rs.core.UriBuilder.fromResource;

@Path("/v5/{userId}/{dataSetId}/upload/table")
public class TabularUpload {

  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final DataSetFactory dataSetFactory;
  private final ConcurrentHashMap<UUID, StringBuffer> status;

  public TabularUpload(LoggedInUsers loggedInUsers, Authorizer authorizer, DataSetFactory dataSetFactory) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.dataSetFactory = dataSetFactory;
    this.status = new ConcurrentHashMap<>();
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("file") final FormDataContentDisposition fileInfo,
                         @FormDataParam("type") final String fileType,
                         FormDataMultiPart formData,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("userId") final String userId,
                         @PathParam("dataSetId") final String dataSetId)
    throws DataStoreCreationException, FileStorageFailedException, ExecutionException, InterruptedException,
    LogStorageFailedException {
    }

    // final Response response = checkWriteAccess(authorizer, loggedInUsers, authHeader, userId, dataSetId);
    // if (response != null) {
    //   return response;
    // }

    DataSet dataSet = dataSetFactory.createDataSet(userId, dataSetId);

    String fileToken = dataSet.addFile(
      rdfInputStream,
      fileInfo.getName(),
      Optional.of(body.getMediaType())
    );

    Loader loader = LoaderFactory.createFor(configFromFormData(formData));

    UUID importId = UUID.randomUUID();
    StringBuffer importStatusConsumer = new StringBuffer();
    status.put(importId, importStatusConsumer);

    dataSet.generateLog(
      UriBuilder.fromUri("http://timbuctoo.huygens.knaw.nl").path(userId).path(dataSetId).path(fileToken).build(),
      new TabularRdfCreator(dataSet, loader, dataSetId, fileInfo.getFileName(), importStatusConsumer::append, fileToken)
    );

    return Response.created(fromResource(TabularUpload.class)
      .path(importId.toString())
      .buildFromMap(ImmutableMap.of("userId", userId, "dataSetId", dataSetId))
    ).build();
  }

  @GET
  @Path("{importId}")
  public Response getStatus(@PathParam("importId") final UUIDParam importId) {
    return Response.ok(status.get(importId.get())).build();
  }

  private LoaderConfig configFromFormData(FormDataMultiPart formData) {
    FormDataBodyPart typeField = formData.getField("type");
    String typeString = typeField != null ? typeField.getValue() : "xlsx";

    if (typeString.equals("csv")) {
      Map<String, String> extraConfig = formData.getFields().entrySet().stream()
                                                .filter(entry -> !entry.getKey().equals("file"))
                                                .filter(entry -> !entry.getKey().equals("vreId"))
                                                .filter(entry -> !entry.getKey().equals("uploadType"))
                                                .filter(entry -> entry.getValue().size() > 0 &&
                                                  entry.getValue().get(0) != null)
                                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                  entry -> entry.getValue().get(0).getValue()));
      return LoaderConfig.csvConfig(extraConfig);
    }

    return LoaderConfig.configFor(typeString);


  }

}
