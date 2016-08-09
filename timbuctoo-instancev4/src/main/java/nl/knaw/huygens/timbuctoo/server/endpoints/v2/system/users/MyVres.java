package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/system/users/me/vres")
public class MyVres {
  private final LoggedInUserStore loggedInUserStore;
  private final Authorizer authorizer;
  private final Vres vres;
  private final SaveRml saveRml;

  public MyVres(LoggedInUserStore loggedInUserStore, Authorizer authorizer, Vres vres, SaveRml saveRml) {
    this.loggedInUserStore = loggedInUserStore;
    this.authorizer = authorizer;
    this.vres = vres;
    this.saveRml = saveRml;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@HeaderParam("Authorization") String authorizationHeader) {
    Optional<User> user = loggedInUserStore.userFor(authorizationHeader);

    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    final Map<String, Map<String, ObjectNode>> result = vres.getVres().values().stream()
      .map(vre -> {
        boolean isAllowedToWrite;
        try {
          isAllowedToWrite = authorizer
            .authorizationFor(vre.getVreName(), user.get().getId())
            .isAllowedToWrite();
        } catch (AuthorizationUnavailableException e) {
          isAllowedToWrite = false;
        }
        boolean isPublished = vre.getCollections().size() > 0;
        return new VreJson(vre.getVreName(), isAllowedToWrite, isPublished);
      })
      .filter(x -> x.isMine() || x.isPublished())
      .collect(groupingBy(
        x -> x.isMine() ? "mine" : "public",
        mapping(x -> {
          if (x.isMine()) {
            return jsnO(
              "name", jsn(x.getVreName()),
              "published", jsn(x.isPublished),
              "rmlUri", jsn(x.getRmlUri())
            );
          } else {
            return jsnO(
              "name", jsn(x.getVreName())
            );
          }
        }, toMap(x -> x.get("name").asText(), x-> x))));

    return Response.ok(result).build();
  }

  private class VreJson {
    private final boolean isMine;
    private final boolean isPublished;
    private final String vreName;

    public VreJson(String vreName, boolean isMine, boolean isPublished) {
      this.vreName = vreName;
      this.isMine = isMine;
      this.isPublished = isPublished;
    }

    public String getVreName() {
      return vreName;
    }

    public boolean isMine() {
      return isMine;
    }

    public boolean isPublished() {
      return isPublished;
    }

    public String getRmlUri() {
      return saveRml.makeUri(vreName).toASCIIString();
    }
  }
}
