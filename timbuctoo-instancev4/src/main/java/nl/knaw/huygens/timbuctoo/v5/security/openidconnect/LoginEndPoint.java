package nl.knaw.huygens.timbuctoo.v5.security.openidconnect;


import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/v5/openid-connect")
public class LoginEndPoint {
  public static final Logger LOG = LoggerFactory.getLogger(LoginEndPoint.class);
  private final String clientId;
  private final String clientSecret;
  private final String authorizationUri;
  private final String tokenUri;
  private final Map<UUID, String> loginSessionRedirects;
  private final String redirectUrl;
  private final String userInfoUrl;

  public LoginEndPoint(String clientId, String clientSecret, String authorizationUri, String tokenUri,
                       String redirectUrl, String userInfoUrl) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.authorizationUri = authorizationUri;
    this.tokenUri = tokenUri;
    this.redirectUrl = redirectUrl;
    loginSessionRedirects = new HashMap<>();
    this.userInfoUrl = userInfoUrl;
  }


  @GET
  @Path("/login")
  public Response login(@QueryParam("redirect-uri") String clientRedirectUri) {
    LOG.info("login");
    if (StringUtils.isBlank(clientRedirectUri)) {
      return Response.status(400).entity("expected a query param redirect-uri").build();
    }

    UUID sessionId = UUID.randomUUID();

    loginSessionRedirects.put(sessionId, clientRedirectUri);

    final URI openIdServer = UriBuilder.fromUri(authorizationUri)
                                       .queryParam("response_type", "code")
                                       .queryParam("client_id", clientId)
                                       .queryParam("redirect_uri", redirectUrl)
                                       .queryParam("scope", "openid email profile schac_home_organisation")
                                       .queryParam("state", sessionId)
                                       .build();

    return Response.status(308).location(openIdServer).build();
  }


  @GET
  @Path("/callback")
  public Response callback(@QueryParam("state") UUID loginSession, @QueryParam("code") String code) {
    if (!loginSessionRedirects.containsKey(loginSession)) {
      return Response.status(417).entity("Login session unknown").build();
    }
    final URI timRedirectUri = UriBuilder.fromUri(redirectUrl).build();

    final ClientAuthentication basicAuth = new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret));
    try {
      final URI tokenEndpoint = new URI(tokenUri);
      final AuthorizationCodeGrant authzGrant = new AuthorizationCodeGrant(new AuthorizationCode(code), timRedirectUri);
      final TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, basicAuth, authzGrant);
      final TokenResponse response = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

      if (response.indicatesSuccess()) {
        final Tokens tokens = response.toSuccessResponse().getTokens();

        LOG.info("token request success: {}", tokens);

        // TODO check if the id is not fake

        final URI userInfoUri = UriBuilder.fromUri(userInfoUrl).build();
        final UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoUri, tokens.getBearerAccessToken());
        final UserInfoResponse userInfoResponse = UserInfoResponse.parse(userInfoRequest.toHTTPRequest().send());

        if (userInfoResponse.indicatesSuccess()) {
          final UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

          // TODO store user in loggedin users

          LOG.info("User info request success: {}", userInfo.toJSONObject());
        } else {
          LOG.error("User info request failed: {}", userInfoResponse.toErrorResponse().getErrorObject());
        }

      } else {
        LOG.error("Token request failed: {}", response.toErrorResponse().getErrorObject());
      }

    } catch (URISyntaxException | IOException | ParseException e) {
      LOG.error("Token request threw an exception", e);
    }


    final URI userUri = UriBuilder.fromUri(loginSessionRedirects.get(loginSession))
                                  .queryParam("sessionToken", code)
                                  .build();
    return Response.temporaryRedirect(userUri).build();
  }


}
