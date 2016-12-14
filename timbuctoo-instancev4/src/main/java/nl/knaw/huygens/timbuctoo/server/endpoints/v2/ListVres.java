package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
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

  private final UriHelper uriHelper;
  private TransactionEnforcer transactionEnforcer;

  public ListVres(UriHelper uriHelper, TransactionEnforcer transactionEnforcer) {
    this.uriHelper = uriHelper;
    this.transactionEnforcer = transactionEnforcer;
  }

  @GET
  public Response get() {
    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      final ArrayNode result = jsnA(timbuctooActions.loadVres().getVres().values().stream().map(vre -> jsnO(
        "name", jsn(vre.getVreName()),
        "label", jsn(vre.getLabel()),
        "metadata", jsn(createUri(vre.getVreName()).toString()),
        "isPublished", jsn(vre.getPublishState().equals(Vre.PublishState.AVAILABLE))
      )));
      return TransactionStateAndResult.commitAndReturn(Response.ok(result).build());
    });
  }

  private URI createUri(String vreName) {
    return uriHelper.fromResourceUri(UriBuilder.fromResource(Metadata.class).resolveTemplate("vre", vreName).build());
  }
}
