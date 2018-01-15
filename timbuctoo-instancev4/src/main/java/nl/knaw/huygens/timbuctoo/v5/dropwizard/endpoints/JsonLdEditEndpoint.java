package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdError;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.util.LambdaOriginatedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.ConcurrentUpdateException;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper.handleImportManagerResult;
import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;
import static nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonProvenanceToRdfPatch.fromCurrentState;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_JSONLD_UPLOAD_CONTEXT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_USERS;

@Path("/v5/{user}/{dataset}/upload/jsonld")
public class JsonLdEditEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(JsonLdEditEndpoint.class);

  private final DataSetRepository dataSetRepository;
  private final UserValidator userValidator;
  private final PermissionFetcher permissionFetcher;
  private DocumentLoader documentLoader;

  public JsonLdEditEndpoint(UserValidator userValidator, PermissionFetcher permissionFetcher,
                            DataSetRepository dataSetRepository,
                            CloseableHttpClient httpClient
  ) throws JsonLdError, IOException {
    this.dataSetRepository = dataSetRepository;
    this.permissionFetcher = permissionFetcher;
    this.userValidator = userValidator;
    documentLoader = new DocumentLoader();
    documentLoader.setHttpClient(httpClient);
    final String prefilledContext =
      Resources.toString(JsonLdEditEndpoint.class.getResource("/static/v5/jsonLdUploadContext.json"), Charsets.UTF_8);
    documentLoader.addInjectedDoc(
      TIM_JSONLD_UPLOAD_CONTEXT,
      prefilledContext
    );
  }

  @PUT
  public Response submitChanges(String jsonLdImport,
                                @PathParam("user") String ownerId,
                                @PathParam("dataset") String dataSetId,
                                @HeaderParam("authorization") String authHeader)
    throws LogStorageFailedException {
    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(authHeader);
    } catch (UserValidationException e) {
      user = Optional.empty();
    }

    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(user.get(), ownerId, dataSetId);

    if (!dataSetOpt.isPresent()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    final DataSet dataSet = dataSetOpt.get();
    final QuadStore quadStore = dataSet.getQuadStore();
    final ImportManager importManager = dataSet.getImportManager();

    final Response response = checkWriteAccess(dataSet, user, permissionFetcher);
    if (response != null) {
      return response;
    }
    // user is present after checkWriteAccess
    final User currentUser = user.get();
    Supplier<RdfCreator> supplier = () -> {
      try {
        return fromCurrentState(
          documentLoader,
          jsonLdImport,
          quadStore,
          TIM_USERS + currentUser.getPersistentId(),
          UUID.randomUUID().toString(),
          Clock.systemUTC()
        );
      } catch (IOException | ConcurrentUpdateException e) {
        throw new LambdaOriginatedException(e);
      }
    };

    final Future<ImportStatus> promise = importManager.generateLog(
      dataSet.getMetadata().getBaseUri(),
      dataSet.getMetadata().getBaseUri(),
      supplier
    );
    return handleImportManagerResult(promise, (importStatus) -> {
      if (importStatus.getLastError() instanceof ConcurrentUpdateException) {
        return Response.Status.CONFLICT;
      } else {
        return Response.Status.BAD_REQUEST;
      }
    });
  }

}
