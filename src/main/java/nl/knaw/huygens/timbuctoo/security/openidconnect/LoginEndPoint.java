package nl.knaw.huygens.timbuctoo.security.openidconnect;

import com.nimbusds.oauth2.sdk.token.Tokens;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/openid-connect")
public class LoginEndPoint {
  public static final Logger LOG = LoggerFactory.getLogger(LoginEndPoint.class);
  private final Map<UUID, LoginSessionData> loginSessions;
  private final OpenIdClient openIdClient;

  public LoginEndPoint(OpenIdClient openIdClient) {
    this.openIdClient = openIdClient;
    loginSessions = new HashMap<>();
  }

  @GET
  @Path("/login")
  public Response login(@QueryParam("redirect-uri") String clientRedirectUri) {
    LOG.info("login");
    if (StringUtils.isBlank(clientRedirectUri)) {
      return Response.status(400).entity("expected a query param redirect-uri").build();
    }

    UUID sessionId = UUID.randomUUID();
    UUID nonce = UUID.randomUUID();

    loginSessions.put(sessionId, new LoginSessionData(clientRedirectUri, nonce.toString()));
    return openIdClient.createRedirectResponse(sessionId, nonce);
  }

  @GET
  @Path("/callback")
  public Response callback(@QueryParam("state") UUID loginSession, @QueryParam("code") String code) {
    if (!loginSessions.containsKey(loginSession)) {
      return Response.status(417).entity("Login session unknown").build();
    }

    try {
      final LoginSessionData loginSessionData = loginSessions.remove(loginSession);
      final Tokens userTokens = openIdClient.getUserTokens(code, loginSessionData.nonce());
      final URI userUri = UriBuilder.fromUri(loginSessionData.userRedirectUri())
                                    .queryParam("sessionToken", userTokens.getBearerAccessToken().getValue())
                                    .build();
      return Response.temporaryRedirect(userUri).build();
    } catch (OpenIdConnectException e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().build();
    }
  }

  private record LoginSessionData(String userRedirectUri, String nonce) {
  }
}
