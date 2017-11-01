package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult.commitAndReturn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/system/vres/{vre}")
public class SingleVre {

  public static final Logger LOG = LoggerFactory.getLogger(SingleVre.class);
  private final UserPermissionChecker permissionChecker;
  private final TransactionEnforcer transactionEnforcer;
  private final VreAuthorizationCrud vreAuthorization;

  public SingleVre(UserPermissionChecker permissionChecker, TransactionEnforcer transactionEnforcer,
                   VreAuthorizationCrud vreAuthorization) {
    this.permissionChecker = permissionChecker;
    this.transactionEnforcer = transactionEnforcer;
    this.vreAuthorization = vreAuthorization;
  }

  @DELETE
  @Produces(APPLICATION_JSON)
  public Response delete(@PathParam("vre") String vreName, @HeaderParam("Authorization") String authorizationHeader) {
    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    final Optional<User> user = permissionChecker.getUserFor(authorizationHeader);

    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      try {
        timbuctooActions.deleteVre(vreName, user.get());
        vreAuthorization.deleteVreAuthorizations(vreName, user.get());
        return commitAndReturn(Response.ok(jsnO("success", jsn(true))).build());
      } catch (AuthorizationException | PermissionFetchingException e) {
        LOG.error("User with id '" + user.get().getId() + "' was not allowed to delete VRE '" + vreName + "'", e);
        return commitAndReturn(
          Response.status(Response.Status.FORBIDDEN).entity(jsnO("success", jsn(false))).build()
        );
      } catch (AuthorizationUnavailableException e) {
        LOG.error("Failed to remove authorization for vre '{}'", vreName, e);
        return commitAndReturn(
          Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsnO("success", jsn(false))).build()
        );
      }
    });

  }
}
