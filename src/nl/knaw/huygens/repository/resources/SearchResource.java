package nl.knaw.huygens.repository.resources;

import java.util.List;

import javax.ws.rs.DefaultValue;
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
  // TODO decide: which request are made persistent? isn't all overkill?
  public List<? extends Document> doSearch( //
      @QueryParam("type") String typeString, //
      @QueryParam("q") String q, //
      @QueryParam("sort") @DefaultValue("id") String sort) //
  {
    if (typeString == null || q == null) {
      // TODO decide: is throwing an exception the proper approach?
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Class<? extends Document> type = docTypeRegistry.getClassFromTypeString(typeString);
    if (type == null) {
      // TODO decide: is throwing an exception the proper approach?
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    try {
      // TODO decide: the rule id --> core is implicit, is this what we want?
      String core = docTypeRegistry.getCollectionId(type);
      Search search = searchManager.search(core, q, sort);
      return convert(type, search.getIds());
    } catch (SolrServerException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  private <T extends Document> List<T> convert(Class<T> type, List<String> ids) {
    List<T> list = Lists.newArrayList();
    for (String id : ids) {
      list.add(storageManager.getDocument(type, id));
    }
    return list;
  }

}
