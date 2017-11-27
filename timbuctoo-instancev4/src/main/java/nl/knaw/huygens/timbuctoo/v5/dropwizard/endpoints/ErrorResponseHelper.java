package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class ErrorResponseHelper {
  public Response dataSetNotFound(String userId, String dataSetId) {
    return error(404, "No dataSet found for ownerId: '" + userId + "' and dataSetId: '" + dataSetId + "'.");
  }

  public Response error(int status, String error) {
    return Response
      .status(status)
      .entity(
        jsnO(
          "error", jsn(error)
        ).toString()
      )
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }

  public static Response handleImportManagerResult(Future<ImportStatus> promise) {
    try {
      final ImportStatus status = promise.get();
      if (!status.hasErrors()) {
        return Response
          .status(Response.Status.CREATED)
          .build();
      } else {
        return Response
          .status(Response.Status.BAD_REQUEST)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(status)
          .build();
      }
    } catch (InterruptedException | ExecutionException e) {
      return Response.serverError().build();
    }

  }

}
