package nl.knaw.huygens.repository.resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Sitemap;
import nl.knaw.huygens.repository.util.APIDesc;


@Path("api")
public class SitemapResource {
  private static Sitemap lastSitemap;

  @GET
  @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
  @APIDesc("Generates a structured sitemap.")
  public Sitemap getSitemap(@QueryParam("refresh") boolean shouldRefresh, @Context Application app) {
    if (shouldRefresh || lastSitemap == null) {
      lastSitemap = new Sitemap(app);
    }
    return lastSitemap;
  }
}
