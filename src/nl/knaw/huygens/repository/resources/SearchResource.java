package nl.knaw.huygens.repository.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.managers.SearchManager;
import nl.knaw.huygens.repository.model.Search;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.util.APIDesc;

import org.apache.solr.client.solrj.SolrServerException;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path("search")
public class SearchResource {

  @Inject
  private SearchManager manager;

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  @APIDesc("Searches the Solr index.")
  public Search handlePostJson(@QueryParam("q") String term, @QueryParam("core") String core, @QueryParam("sort") String sort) {
    System.err.println(" term = " + term);
    System.err.println(" core = " + core);
    System.err.println(" sort = " + sort);
    try {
      return manager.search(term, sort, core);
    } catch (SolrServerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new Search(new ArrayList<String>(), core, term, sort, new Date().toString());
  }

}
