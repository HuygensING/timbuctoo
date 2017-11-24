package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

  public static Response handleImportManagerResult(Future<List<Throwable>> promise) {
    try {
      final List<Throwable> errorList = promise.get();
      if (errorList.isEmpty()) {
        return Response
          .status(Response.Status.CREATED)
          .build();
      } else {
        return Response
          .status(Response.Status.BAD_REQUEST)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(errorList.stream()
            .map(Throwable::getMessage).collect(Collectors.toList()))
          .build();
      }
    } catch (InterruptedException | ExecutionException e) {
      return Response.serverError().build();
    }

  }

}
