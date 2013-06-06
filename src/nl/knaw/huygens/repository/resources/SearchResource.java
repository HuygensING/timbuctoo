package nl.knaw.huygens.repository.resources;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.SearchManager;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.util.APIDesc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path("search")
public class SearchResource {

  private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

  @Inject
  private SearchManager searchManager;
  @Inject
  private StorageManager storageManager;
  @Inject
  private DocTypeRegistry docTypeRegistry;

  @POST
  @APIDesc("Searches the Solr index")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public String step1( //
      @QueryParam("type") @DefaultValue("person") String typeString, //
      @QueryParam("q") String q, //
      @QueryParam("sort") @DefaultValue("id") String sort //
  ) {

    // Validate input
    if (typeString == null || q == null) {
      LOG.warn("POST - type: '{}', q: '{}'", typeString, q);
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Class<? extends Document> type = docTypeRegistry.getClassFromWebServiceTypeString(typeString);
    if (type == null) {
      LOG.warn("POST - no type '{}'");
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    // Process
    try {
      String core = docTypeRegistry.getCollectionId(type);
      SearchResult search = searchManager.search(core, q, sort);
      storageManager.addDocument(SearchResult.class, search);
      String queryId = search.getId();
      return String.format("{\"queryId\": \"%s\"}", queryId);
    } catch (Exception e) {
      LOG.warn("POST - {}", e.getMessage());
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @APIDesc("Returns (paged) search results")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<? extends Document> step2( //
      @QueryParam("id") String queryId, //
      @QueryParam("start") @DefaultValue("0") int start, //
      @QueryParam("rows") @DefaultValue("10") int rows //
  ) {

    // Validate input
    if (queryId == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    // Retrieve result
    //    SearchResult result = storageManager.getDocument(SearchResult.class, queryId);
    //    if (result == null) {
    //      throw new WebApplicationException(Response.Status.NOT_FOUND);
    //    }

    // Process
    Class<? extends Document> type = Person.class;
    List<String> ids = Lists.newArrayList("PER0000005354", "PER0000005355", "PER0000005356");
    int lo = toRange(start, 0, ids.size());
    int hi = toRange(lo + rows, 0, ids.size());
    return convert(type, ids, lo, hi);
  }

  private int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private <T extends Document> List<T> convert(Class<T> type, List<String> ids, int lo, int hi) {
    List<T> list = Lists.newArrayList();
    for (int index = lo; index < hi; index++) {
      String id = ids.get(index);
      list.add(storageManager.getDocument(type, id));
    }
    return list;
  }

}
