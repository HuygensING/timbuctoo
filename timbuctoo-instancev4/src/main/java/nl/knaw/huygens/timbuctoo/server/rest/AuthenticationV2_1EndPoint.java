package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.LocalLoginUnavailableException;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.security.BasicAuthorizationHeaderParser;
import nl.knaw.huygens.timbuctoo.server.security.InvalidAuthorizationHeaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/v2.1/authenticate")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationV2_1EndPoint {

  public static final Logger LOG = LoggerFactory.getLogger(AuthenticationV2_1EndPoint.class);
  private final LoggedInUserStore loggedInUserStore;

  public AuthenticationV2_1EndPoint(LoggedInUserStore loggedInUserStore) {
    this.loggedInUserStore = loggedInUserStore;
  }

  @POST
  public Response authenticate(@HeaderParam(HttpHeaders.AUTHORIZATION) String encodedAuthString) {
    try {
      if (encodedAuthString == null) {
        return unauthorizedResponse();
      }
      BasicAuthorizationHeaderParser.Credentials credentials = BasicAuthorizationHeaderParser
        .parse(encodedAuthString);

      Optional<String> token = loggedInUserStore.userTokenFor(credentials.getUsername(), credentials.getPassword());
      if (token.isPresent()) {
        return Response.noContent().header("X_AUTH_TOKEN", token.get()).build();
      } else {
        return unauthorizedResponse();
      }
    } catch (InvalidAuthorizationHeaderException e) {
      LOG.info(e.getMessage());
      return unauthorizedResponse();
    } catch (LocalLoginUnavailableException | AuthenticationUnavailableException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  private Response unauthorizedResponse() {
    return Response
      .status(Response.Status.UNAUTHORIZED)
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"timbuctoo\"")
      .build();
  }
}
