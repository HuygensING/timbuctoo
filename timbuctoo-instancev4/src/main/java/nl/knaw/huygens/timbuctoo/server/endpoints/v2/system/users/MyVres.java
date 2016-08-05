package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v2.1/system/users/me/vres")
public class MyVres {
  private final LoggedInUserStore loggedInUserStore;
  private final Authorizer authorizer;
  private final Vres vres;

  public MyVres(LoggedInUserStore loggedInUserStore, Authorizer authorizer, Vres vres) {
    this.loggedInUserStore = loggedInUserStore;
    this.authorizer = authorizer;
    this.vres = vres;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@HeaderParam("Authorization") String authorizationHeader) {
    Optional<User> user = loggedInUserStore.userFor(authorizationHeader);

    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    List<VreRole> vreRoleList = vres.getVres().values().stream()
                                    .filter(vre -> {
                                      try {
                                        Authorization authorization =
                                          authorizer.authorizationFor(vre.getVreName(), user.get().getId());
                                        return authorization.isAllowedToWrite();
                                      } catch (AuthorizationUnavailableException e) {
                                        return false;
                                      }
                                    })
                                    .map(vre -> {
                                      try {
                                        Authorization authorization =
                                          authorizer.authorizationFor(vre.getVreName(), user.get().getId());
                                        return new VreRole(vre.getVreName(), authorization.getRoles());
                                      } catch (AuthorizationUnavailableException e) {
                                        return new VreRole(vre.getVreName(),
                                          Lists.newArrayList("Role could not be retrieved."));
                                      }
                                    })
                                    .collect(toList());

    return Response.ok(vreRoleList).build();

  }

  private static class VreRole {
    private String vreName;
    private List<String> userRoles;

    public VreRole(String vreName, List<String> userRoles) {
      this.vreName = vreName;
      this.userRoles = userRoles;
    }

    public List<String> getUserRoles() {
      return userRoles;
    }

    public String getVreName() {
      return vreName;
    }
  }
}
