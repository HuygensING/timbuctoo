package nl.knaw.huygens.repository.resources;

import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.storage.Storage;
import nl.knaw.huygens.repository.util.APIDesc;
import nl.knaw.huygens.repository.util.JAXUtils;
import nl.knaw.huygens.repository.util.JAXUtils.API;

import com.google.common.collect.Lists;

@Path("api")
public class SitemapResource {
  public static class Sitemap extends Document {
    public List<API> availableAPIList;
    
    public Sitemap(Application app) {
      setType("sitemap");
      setId(String.format("T%d", System.currentTimeMillis()));
      List<API> rv = Lists.newArrayList();
      Set<Class<?>> allResources = JAXUtils.getAllResources(app);
      for (Class<?> cls : allResources) {
        List<API> generatedAPIs = JAXUtils.generateAPIs(cls);
        if (generatedAPIs != null) {
          rv.addAll(generatedAPIs);
        }
      }
      availableAPIList = rv;
    }

    @Override
    public String getDescription() {
      return "Repository Sitemap";
    }

    @Override
    public void fetchAll(Storage storage) {
      // No-op;
    }
  }

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
