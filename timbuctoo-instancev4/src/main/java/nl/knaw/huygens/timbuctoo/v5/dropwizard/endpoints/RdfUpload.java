package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSet;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.FileSystemBasedDataSetManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogMetadata;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.rdf4j.Rdf4jRdfParser;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Path("/v4/rdf-upload/{dataSet}")
public class RdfUpload {

  protected final ImportManager importManager;
  private final LoggedInUsers loggedInUsers;
  private final FileSystemBasedDataSetManager dataSetManager;


  public RdfUpload(DataStoreFactory dataStoreFactory, LoggedInUsers loggedInUsers,
                   FileSystemBasedDataSetManager dataSetManager) {
    this.loggedInUsers = loggedInUsers;
    this.dataSetManager = dataSetManager;
    NoOpLogMetadata noOp = new NoOpLogMetadata();
    importManager = new ImportManager(noOp, noOp, new Rdf4jRdfParser(), dataStoreFactory);
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public Response upload(@FormDataParam("file") final InputStream rdfInputStream,
                         @FormDataParam("file") final FormDataBodyPart body,
                         @FormDataParam("encoding") final String encoding,
                         @FormDataParam("uri") final URI uri,
                         @HeaderParam("authorization") final String authHeader,
                         @PathParam("dataSet") final String dataSetId)
    throws IOException, LogProcessingFailedException, ExecutionException, InterruptedException {

    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    DataSet dataSet = dataSetManager.getsert(user.get().getPersistentId(), dataSetId);

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

  private class NoOpLogMetadata implements LogMetadata, LogStorage {

    @Override
    public void addLog(String dataSet, URI logUri) {

    }

    @Override
    public LocalLog startOrContinueAppendLog(String dataSet) {
      return null;
    }

    @Override
    public void appendToLogFinished(String dataSet) {

    }

    @Override
    public QuadHandler startWritingToLog(LocalLog log) {
      return null;
    }

    @Override
    public void writeFinished(String dataSet) {

    }
  }
}
