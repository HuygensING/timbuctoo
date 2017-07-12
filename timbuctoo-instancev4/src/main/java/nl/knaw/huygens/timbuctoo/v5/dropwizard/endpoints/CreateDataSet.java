package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;

@Path("/v5/dataSets/{userId}/{dataSetId}/create")
public class CreateDataSet {
  private final LoggedInUsers loggedInUsers;
  private final DataSetFactory dataSetFactory;

  public CreateDataSet(LoggedInUsers loggedInUsers, DataSetFactory dataSetFactory) {
    this.loggedInUsers = loggedInUsers;
    this.dataSetFactory = dataSetFactory;
  }

  @POST
  public Response create(@PathParam("userId") String userId, @PathParam("dataSetId") String dataSetId,
                         @HeaderParam("Authorization") String authorization)
    throws DataStoreCreationException {
    Optional<User> user = loggedInUsers.userFor(authorization);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String persistentId = user.get().getPersistentId();
    if (!Objects.equals(persistentId, userId)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    dataSetFactory.createImportManager(persistentId, dataSetId);


    return Response.created(DataSet.makeUrl(persistentId, dataSetId)).build();
  }
}
