package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;

@Path("/v2.1/javascript-globals")
@Produces("application/javascript")
public class JsEnv {

  private final TimbuctooConfiguration configuration;

  public JsEnv(TimbuctooConfiguration configuration) {
    this.configuration = configuration;
  }

  @GET
  public String get() {
    return "var globals = " + jsnO("env", jsnO("TIMBUCTOO_SEARCH_URL",
      jsn(configuration.getTimbuctooSearchUrl()))).toString();
  }
}
