package nl.knaw.huygens.timbuctoo.server.endpoints;

import nl.knaw.huygens.timbuctoo.util.UriHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/")
public class RootEndpoint {
  private final UriHelper uriHelper;
  private final Optional<URI> userRedirectUrl;
  private final URI loginRedirectUrl;

  public RootEndpoint(UriHelper uriHelper, Optional<URI> userRedirectUrl, URI loginRedirectUrl) {
    this.uriHelper = uriHelper;
    this.userRedirectUrl = userRedirectUrl;
    this.loginRedirectUrl = loginRedirectUrl;
  }

  @GET
  public Response getHomepage() {
    URI url = userRedirectUrl.orElseGet(() -> uriHelper.fromResourceUri(URI.create("/static/upload/")));
    return Response.temporaryRedirect(url).build();
  }

  @GET
  @Path("/login")
  public Response getLoginRedirect(@Context UriInfo uriInfo) {
    UriBuilder redirectUriBuilder = UriBuilder.fromUri(loginRedirectUrl);
    for (Map.Entry<String, List<String>> queryParam : uriInfo.getQueryParameters().entrySet()) {
      redirectUriBuilder.queryParam(queryParam.getKey(), queryParam.getValue().toArray());
    }
    return Response.temporaryRedirect(redirectUriBuilder.build()).build();
  }
}
