package nl.knaw.huygens.timbuctoo.security.dummy;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

@Path("/dummylogin")
public class DummyLoginEndPoint {
  @GET
  public Response login(@QueryParam("redirect-uri") String clientRedirectUri) {
    final String redirectUri = !StringUtils.isBlank(clientRedirectUri) ? clientRedirectUri : "/";
    final URI redirectUriWithToken = UriBuilder.fromUri(redirectUri)
        .queryParam("sessionToken", UUID.randomUUID())
        .build();
    return Response.temporaryRedirect(redirectUriWithToken).build();
  }
}
