package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/v5/dataSets/{userId}/{dataSetId}/create")
public class CreateDataSet {
  private final AuthCheck authCheck;

  public CreateDataSet(AuthCheck authCheck) {
    this.authCheck = authCheck;
  }

  @POST
  public Response create(@PathParam("userId") String userId, @PathParam("dataSetId") String dataSetId,
                         @HeaderParam("Authorization") String authorization)
    throws DataStoreCreationException {

    final Either<Response, Response> result = authCheck.getOrCreate(authorization, userId, dataSetId, true)
      .map(ds -> {
        final PromotedDataSet metadata = ds.getRight().getMetadata();
        return Response.created(DataSet.makeUrl(metadata.getOwnerId(), metadata.getDataSetId())).build();
      });

    if (result.isLeft()) {
      return result.getLeft();
    } else {
      return result.get();
    }
  }
}
