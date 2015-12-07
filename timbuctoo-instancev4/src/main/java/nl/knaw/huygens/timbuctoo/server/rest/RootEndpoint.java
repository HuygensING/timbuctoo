package nl.knaw.huygens.timbuctoo.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/")
public class RootEndpoint {
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getHomepage() {
    InputStream indexPage = Thread.currentThread()//
      .getContextClassLoader().getResourceAsStream("index.html");
    return Response.ok(indexPage).build();
  }
}
