package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.LoaderFactory.LoaderConfig;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.Saver;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;

@Path("/v5/{userId}/{dataSetId}/upload/table")
public class TabularUpload {

  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final DataSetFactory dataSetManager;

  public TabularUpload(LoggedInUsers loggedInUsers, Authorizer authorizer, DataSetFactory dataSetManager) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.dataSetManager = dataSetManager;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("fileUpload") final FormDataContentDisposition fileInfo,
                         @FormDataParam("type") final String filetype,
                         FormDataMultiPart formData,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("userId") final String userId,
                         @PathParam("dataSetId") final String dataSetId)
    throws DataStoreCreationException, FileStorageFailedException, ExecutionException, InterruptedException {
    final Response response = checkWriteAccess(authorizer, loggedInUsers, authHeader, userId, dataSetId);
    if (response != null) {
      return response;
    }

    DataSet dataSet = dataSetManager.createDataSet(userId, dataSetId);

    String fileToken = dataSet.addFile(
      rdfInputStream,
      fileInfo.getName(),
      Optional.of(MediaType.valueOf(filetype))
    );

    Loader loader = LoaderFactory.createFor(fromFormData(formData));

    Future<?> promise = dataSet.generateLog(
      UriBuilder.fromUri("http://timbuctoo.huygens.knaw.nl").path(userId).path(dataSetId).path(fileToken).build(),
      saver -> { //FIXME make rdf serializable (store file token and loaderConfig
        // Loader loader = LoaderFactory.createFor(filetype, loaderConfig);
        loader.loadData(getFile(fileToken), new Importer(new StateMachine(new Saver() {
          @Override
          public Vertex addEntity(Vertex collection, Map<String, ?> currentProperties) {

          }

          @Override
          public Vertex addCollection(String collectionName) {

          }

          @Override
          public void addPropertyDescriptions(Vertex collection,
                                              ImportPropertyDescriptions importPropertyDescriptions) {

          }
        }), InMemoryStatusTracker));
        //FIXME: hoe doen we status reports?
        //reporter houdt een in memory objectje bij
        //de client kan GET requests doen om dat objectje uit te lezen
      }
    );
    promise.get();

    return Response.ok(fileToken).build();
  }

  private LoaderConfig fromFormData(FormDataMultiPart formData) {
    FormDataBodyPart typeField = formData.getField("type");
    String typeString = typeField != null ? typeField.getValue() : "xlsx";

    if(typeString.equals("csv")) {
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

  private Optional<MediaType> getMediaType(FormDataBodyPart body) {
    return body == null || body.getMediaType() == null ?
      Optional.empty() :
      Optional.of(body.getMediaType());
  }

}
