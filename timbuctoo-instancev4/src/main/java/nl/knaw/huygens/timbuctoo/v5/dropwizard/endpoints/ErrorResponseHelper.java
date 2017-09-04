package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

}
