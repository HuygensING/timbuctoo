package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.UriHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/system/vres")
@Produces(MediaType.APPLICATION_JSON)
public class ListVres {

  private final Vres vres;
  private final UriHelper uriHelper;

  public ListVres(Vres vres, UriHelper uriHelper) {
    this.vres = vres;
    this.uriHelper = uriHelper;
  }

  @GET
  public Response get() {
    final ArrayNode result = jsnA(vres.getVres().values().stream().map(vre -> jsnO(
      "name", jsn(vre.getVreName()),
      "metadata", jsn(createUri(vre.getVreName()).toString()),
      "isPublished", jsn(vre.getCollections().size() > 0)
    )));
    return Response.ok(result).build();
  }

  private URI createUri(String vreName) {
    return uriHelper.fromResourceUri(UriBuilder.fromResource(Metadata.class).resolveTemplate("vre", vreName).build());
  }
}
