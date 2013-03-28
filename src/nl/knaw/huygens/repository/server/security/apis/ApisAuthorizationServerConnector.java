package nl.knaw.huygens.repository.server.security.apis;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

import org.apache.commons.codec.binary.Base64;
import org.surfnet.oaaas.auth.principal.AuthenticatedPrincipal;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class ApisAuthorizationServerConnector implements OAuthAuthorizationServerConnector {
  private String serverURL;
  private String key;
  private String secret;

  @Inject
  public ApisAuthorizationServerConnector(@Named("apis.server") String serverURL, @Named("apis.key") String key, @Named("apis.secret") String secret) {
    this.serverURL = serverURL;
    this.key = key;
    this.secret = secret;
  }

  @Override
  public SecurityContext authenticate(String oAuthUserKey) {
    String userKey = trimAuthorizationKey(oAuthUserKey);

    String auth = "Basic ".concat(new String(Base64.encodeBase64(key.concat(":").concat(secret).getBytes())));

    ApisAuthorizer authorizer = new ApisAuthorizer();

    Client client = Client.create();
    ClientResponse clientResponse = client.resource(String.format(serverURL, userKey)).header(HttpHeaders.AUTHORIZATION, auth).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    if (clientResponse.getClientResponseStatus() == ClientResponse.Status.OK) {
      VerifyTokenResponse verifyTokenResponse = clientResponse.getEntity(VerifyTokenResponse.class);
      AuthenticatedPrincipal principal = verifyTokenResponse.getPrincipal();
      authorizer.setPrincipal(principal);
    }
    return authorizer;
  }

  private String trimAuthorizationKey(String key) {
    String returnKey = null;
    if (key == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    returnKey = key.replace("bearer ", "");
    return returnKey.trim();
  }
}
