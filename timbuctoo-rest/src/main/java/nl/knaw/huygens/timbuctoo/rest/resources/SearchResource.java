package nl.knaw.huygens.timbuctoo.rest.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.solr.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path("search")
public class SearchResource {

  private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

  @Inject
  private Configuration config;
  @Inject
  private TypeRegistry registry;
  @Inject
  private StorageManager storageManager;
  @Inject
  private SearchManager searchManager;

  @POST
  @APIDesc("Searches the Solr index")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response post(FacetedSearchParameters searchParams) {
    // TODO determine scope dynamically
    Scope scope = config.getDefaultScope();
    String typeString = searchParams.getTypeString();
    String q = searchParams.getTerm();

    // Validate input
    if (typeString == null || q == null) {
      LOG.error("POST - type: '{}', q: '{}'", typeString, q);
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Class<? extends Entity> type = registry.getTypeForIName(typeString);
    if (type == null) {
      LOG.error("POST - no such type: {}", typeString);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    // Process
    try {
      SearchResult result = searchManager.search(scope, type, searchParams);
      storageManager.addEntity(SearchResult.class, result);
      String queryId = result.getId();
      return Response.created(new URI(queryId)).build();
    } catch (NoSuchFacetException e) {
      LOG.warn("POST - no such facet: {}", e.getMessage());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    } catch (Exception e) {
      LOG.warn("POST - {}", e.getMessage());
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/{id: " + SearchResult.ID_PREFIX + "\\d+}")
  @APIDesc("Returns (paged) search results")
  @Produces({ MediaType.APPLICATION_JSON })
  @JsonView(JsonViews.WebView.class)
  public Response get( //
      @PathParam("id") String queryId, //
      @QueryParam("start") @DefaultValue("0") final int start, //
      @QueryParam("rows") @DefaultValue("10") final int rows, //
      @Context UriInfo uriInfo) {

    // Retrieve result
    SearchResult result = storageManager.getEntity(SearchResult.class, queryId);
    if (result == null) {
      LOG.error("GET - no results for id '{}'", queryId);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    // Process
    Class<? extends Entity> type = registry.getTypeForIName(result.getSearchType());
    if (type == null) {
      LOG.error("GET - no entity type for '{}'", result.getSearchType());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    List<String> ids = result.getIds();
    int lo = toRange(start, 0, ids.size());
    int hi = toRange(lo + rows, 0, ids.size());

    List<String> idsToGet = ids.subList(lo, hi);
    Set<String> sortableFields = searchManager.findSortableFields(type);

    Map<String, Object> returnValue = Maps.newHashMap();
    returnValue.put("term", result.getTerm());
    returnValue.put("facets", result.getFacets());
    returnValue.put("numFound", ids.size());
    returnValue.put("ids", idsToGet);
    returnValue.put("results", retrieve(type, ids, lo, hi));
    returnValue.put("start", lo);
    returnValue.put("rows", idsToGet.size());
    returnValue.put("sortableFields", sortableFields);

    LOG.debug("path: {}", uriInfo.getAbsolutePath());

    if (start > 0) {
      UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
      int prevStart = Math.max(start - rows, 0);
      uriBuilder.queryParam("start", prevStart).queryParam("rows", rows);
      returnValue.put("_prev", uriBuilder.build());
    }

    if (hi < ids.size()) {
      UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
      uriBuilder.queryParam("start", start + rows).queryParam("rows", rows);
      returnValue.put("_next", uriBuilder.build());
    }

    return Response.ok(returnValue).build();
  }

  private int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private <T extends Entity> List<T> retrieve(Class<T> type, List<String> ids, int lo, int hi) {
    List<T> list = Lists.newArrayList();
    // TODO get all at once
    for (int index = lo; index < hi; index++) {
      String id = ids.get(index);
      list.add(storageManager.getEntity(type, id));
    }
    return list;
  }

}
