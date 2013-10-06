package nl.knaw.huygens.repository.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.annotations.APIDesc;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.rest.util.Sitemap;

import com.google.inject.Inject;

@Path("api")
public class SitemapResource {

  private final DocTypeRegistry registry;

  @Inject
  public SitemapResource(DocTypeRegistry registry) {
    this.registry = registry;
  }

  @GET
  @Produces({ MediaType.TEXT_HTML })
  @APIDesc("Generates a structured sitemap.")
  public Sitemap getSitemap(@Context Application app) {
    return new Sitemap(app, registry);
  }

}
