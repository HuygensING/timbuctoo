package nl.knaw.huygens.repository.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.repository.managers.SearchManager;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Search;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.util.APIDesc;

import org.apache.solr.client.solrj.SolrServerException;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("search")
public class SearchResource {

  @Inject
  private SearchManager searchManager;
  @Inject
  private StorageManager storageManager;
  @Inject
  private DocumentTypeRegister docTypeRegistry;

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  @APIDesc("Searches the Solr index.")
  public List<? extends Document> handlePostJson(@QueryParam("q") String term, @QueryParam("type") String typeString, @QueryParam("sort") String sort) {
    System.err.println(" term = " + term);
    System.err.println(" type = " + typeString);
    System.err.println(" sort = " + sort);
    try {
      // TODO make sure typeString is valid
      Class<? extends Document> type = docTypeRegistry.getClassFromTypeString(typeString);
      String core = docTypeRegistry.getCollectionId(type);
      Search search = searchManager.search(term, sort, core);
      return convert(type, search.getIds());
    } catch (SolrServerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  private <T extends Document> List<T> convert(Class<T> type, List<String> ids) {
    List<T> list = Lists.newArrayList();
    for (String id : ids) {
      list.add(storageManager.getDocument(type, id));
    }
    return list;
  }

}
