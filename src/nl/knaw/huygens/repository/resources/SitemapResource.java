package nl.knaw.huygens.repository.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Sitemap;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.util.APIDesc;

import com.google.inject.Inject;

@Path("api")
public class SitemapResource {

  private final DocumentTypeRegister docTypeRegistry;

  @Inject
  public SitemapResource(final DocumentTypeRegister registry) {
    docTypeRegistry = registry;
  }

  @GET
  @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
  @APIDesc("Generates a structured sitemap.")
  public Sitemap getSitemap(@QueryParam("refresh") boolean ignored, @Context Application app) {
    return new Sitemap(app, docTypeRegistry);
  }

}
