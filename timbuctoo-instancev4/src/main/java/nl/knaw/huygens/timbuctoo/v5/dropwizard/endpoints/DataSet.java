package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkAdminAccess;

@Path("/v5/{userId}/{dataSetId}")
public class DataSet {
  private final LoggedInUsers loggedInUsers;
  private final PermissionFetcher permissionFetcher;
  private final DataSetRepository dataSetRepository;

  public DataSet(LoggedInUsers loggedInUsers, PermissionFetcher permissionFetcher,
                 DataSetRepository dataSetRepository) {
    this.loggedInUsers = loggedInUsers;
    this.permissionFetcher = permissionFetcher;
    this.dataSetRepository = dataSetRepository;
  }

  public static URI makeUrl(String userId, String dataSetId) {
    return UriBuilder.fromResource(DataSet.class)
                     .buildFromMap(ImmutableMap.of(
                       "userId", userId,
                       "dataSetId", dataSetId
                     ));
  }
  // TODO make an api description for the dataset

  @DELETE
  public Response delete(@PathParam("userId") String ownerId, @PathParam("dataSetId") String dataSetName,
                         @HeaderParam("authorization") String authorization) {

    Response response = dataSetRepository.getDataSet(ownerId, dataSetName)
      .map(dataSet -> checkAdminAccess(
        permissionFetcher,
        loggedInUsers,
        authorization,
        dataSet.getMetadata()
      ))
      .orElse(Response.status(Response.Status.NOT_FOUND).build());

    if (response.getStatus() != 200) {
      return response;
    }

    try {
      dataSetRepository.removeDataSet(ownerId, dataSetName);
    } catch (IOException e) {
      LoggerFactory.getLogger(DataSet.class).error("Failed to delete data set", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Response.Status.NO_CONTENT).build();
  }
}
