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

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Strings;
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
  public Response post(SearchParameters searchParams) {
    String scopeId = searchParams.getScopeId();
    if (Strings.isNullOrEmpty(scopeId)) {
      LOG.error("POST - no 'scopeId' specified");
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Scope scope = config.getScopeById(scopeId);
    if (scope == null) {
      LOG.error("POST - no such scope: {}", scopeId);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    String typeString = searchParams.getTypeString();
    if (Strings.isNullOrEmpty(typeString)) {
      LOG.error("POST - no 'typeString' specified");
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Class<? extends Entity> type = registry.getTypeForIName(typeString);
    if (type == null) {
      LOG.error("POST - no such type: {}", typeString);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (!TypeRegistry.isDomainEntity(type)) {
      LOG.error("POST - not a domain entity type: {}", typeString);
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    String q = searchParams.getTerm();
    if (Strings.isNullOrEmpty(q)) {
      LOG.error("POST - no 'q' specified");
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    // Process
    try {
      SearchResult result = searchManager.search(scope, TypeRegistry.toDomainEntity(type), searchParams);
      storageManager.addSystemEntity(SearchResult.class, result);
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
    Class<? extends Entity> entityType = registry.getTypeForIName(result.getSearchType());
    if (entityType == null) {
      LOG.error("GET - no entity type for '{}'", result.getSearchType());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if (!TypeRegistry.isDomainEntity(entityType)) {
      LOG.error("GET - not a domain entity type '{}'", result.getSearchType());
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Class<? extends DomainEntity> type = TypeRegistry.toDomainEntity(entityType);

    List<String> ids = result.getIds() != null ? result.getIds() : Lists.<String> newArrayList();
    int idsSize = ids.size();
    int lo = toRange(start, 0, idsSize);
    int hi = toRange(lo + rows, 0, idsSize);

    List<String> idsToGet = ids.subList(lo, hi);
    List<DomainEntity> entities = retrieveEntities(type, idsToGet);
    List<EntityRef> entityRefs = createEntityRefs(entities);
    Set<String> sortableFields = searchManager.findSortableFields(type);

    Map<String, Object> returnValue = Maps.newHashMap();
    returnValue.put("term", result.getTerm());
    returnValue.put("facets", result.getFacets());
    returnValue.put("numFound", idsSize);
    returnValue.put("ids", idsToGet);
    returnValue.put("refs", entityRefs);
    returnValue.put("results", entities);
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

    if (hi < idsSize) {
      UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
      uriBuilder.queryParam("start", start + rows).queryParam("rows", rows);
      returnValue.put("_next", uriBuilder.build());
    }

    return Response.ok(returnValue).build();
  }

  private int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private <T extends DomainEntity> List<DomainEntity> retrieveEntities(Class<T> type, List<String> ids) {
    List<DomainEntity> list = Lists.newArrayList();
    for (String id : ids) {
      T entity = storageManager.getEntity(type, id);
      if (entity != null) {
        list.add(entity);
      } else {
        LOG.warn("Solr index is out of synch. {} {} not in database.", type.getSimpleName(), id);
      }
    }
    return list;
  }

  private List<EntityRef> createEntityRefs(List<DomainEntity> entities) {
    int size = entities.size();
    List<EntityRef> list = Lists.newArrayListWithCapacity(size);
    if (size != 0) {
      Class<? extends DomainEntity> type = entities.get(0).getClass();
      String itype = registry.getINameForType(type);
      String xtype = registry.getXNameForType(type);
      for (DomainEntity entity : entities) {
        list.add(new EntityRef(itype, xtype, entity.getId(), entity.getDisplayName()));
      }
    }
    return list;
  }

}
