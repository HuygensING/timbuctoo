package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Path("/v4/rdf-upload/{dataSet}")
public class RdfUpload {

  private final LoggedInUsers loggedInUsers;
  private final DataSetFactory dataSetManager;


  public RdfUpload(LoggedInUsers loggedInUsers, DataSetFactory dataSetManager) {
    this.loggedInUsers = loggedInUsers;
    this.dataSetManager = dataSetManager;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("encoding") final String encoding,
                         @FormDataParam("uri") final URI uri,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("dataSet") final String dataSetId)
    throws ExecutionException, InterruptedException, LogStorageFailedException, DataStoreCreationException {

    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    DataSet dataSet = dataSetManager.getOrCreate(user.get().getPersistentId(), dataSetId);

    Future<?> promise = dataSet.addLog(
      uri,
      rdfInputStream,
      Optional.of(Charset.forName(encoding)),
      getMediaType(body)
    );

    promise.get();

    return Response.noContent().build();
  }

  private Optional<MediaType> getMediaType(@FormDataParam("file") FormDataBodyPart body) {
    return body == null || body.getMediaType() == null ?
      Optional.empty() :
      Optional.of(body.getMediaType());
  }

}
