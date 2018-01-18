package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatusReport;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

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

  public static Response handleImportManagerResult(Future<ImportStatusReport> promise) {
    return handleImportManagerResult(promise, (isr) -> Response.Status.BAD_REQUEST);
  }

  public static Response handleImportManagerResult(Future<ImportStatusReport> promise,
                                                   Function<ImportStatusReport, Response.Status>
                                                     errorStatusTranslator) {
    try {
      final ImportStatusReport statusReport = promise.get();
      if (statusReport.hasErrors()) {
        return Response
          .status(errorStatusTranslator.apply(statusReport))
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(statusReport)
          .build();
      } else {
        return Response
          .status(Response.Status.CREATED)
          .build();
      }
    } catch (InterruptedException | ExecutionException e) {
      return Response.serverError().build();
    }

  }

}
