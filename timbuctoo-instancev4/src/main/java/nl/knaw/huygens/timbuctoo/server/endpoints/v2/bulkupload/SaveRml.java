package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/rml/save")
public class SaveRml {

  private UriHelper uriHelper;
  private UserPermissionChecker permissionChecker;
  private TransactionEnforcer transactionEnforcer;

  public SaveRml(UriHelper uriHelper, UserPermissionChecker permissionChecker,
                 TransactionEnforcer transactionEnforcer) {

    this.uriHelper = uriHelper;
    this.permissionChecker = permissionChecker;
    this.transactionEnforcer = transactionEnforcer;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(SaveRml.class).resolveTemplate("vre", vreName).build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  @Consumes("application/ld+json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response post(String rdfData, @PathParam("vre") String vreName,
                       @HeaderParam("Authorization") String authorizationHeader) {

    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }


    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      timbuctooActions.saveRmlMappingState(vreName, rdfData);
      return TransactionStateAndResult.commitAndReturn(Response.ok(jsnO("success", jsn(true))).build());
    });
  }
}
