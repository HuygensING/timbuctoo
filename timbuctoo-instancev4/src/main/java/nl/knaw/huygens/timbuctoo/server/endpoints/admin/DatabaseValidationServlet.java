package nl.knaw.huygens.timbuctoo.server.endpoints.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

public class DatabaseValidationServlet extends HttpServlet {

  private BackgroundRunner<ValidationResult> validator;

  public DatabaseValidationServlet(BackgroundRunner<ValidationResult> validator) {
    this.validator = validator;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    return Response.ok(validator.getMostRecentResult()).build();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final Optional<ValidationResult> mostRecentResult = validator.getMostRecentResult();
    if (mostRecentResult.isPresent()) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      String content = objectMapper.writeValueAsString(mostRecentResult);
      resp.setContentType(MediaType.APPLICATION_JSON);
      resp.setContentLength(content.length());
      resp.getWriter().write(content);
    } else {
      resp.setStatus(404);
    }
  }
}
