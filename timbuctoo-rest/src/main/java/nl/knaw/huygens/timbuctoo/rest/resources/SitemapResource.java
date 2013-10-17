package nl.knaw.huygens.timbuctoo.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.rest.util.Sitemap;

import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/api")
public class SitemapResource {

  private final TypeRegistry registry;

  @Inject
  public SitemapResource(TypeRegistry registry) {
    this.registry = registry;
  }

  @GET
  @Produces({ MediaType.TEXT_HTML })
  @APIDesc("Generates a structured sitemap.")
  public Sitemap getSitemap(@Context
  Application app) {
    return new Sitemap(app, registry);
  }

}
