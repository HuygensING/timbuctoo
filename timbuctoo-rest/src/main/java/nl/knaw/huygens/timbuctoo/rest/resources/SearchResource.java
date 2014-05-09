package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path("search")
public class SearchResource {

  private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

  @Inject
  private TypeRegistry registry;
  @Inject
  private StorageManager storageManager;
  @Inject
  private SearchManager searchManager;
  @Inject
  private VREManager vreManager;
  @Inject
  private Configuration config;

  @GET
  @Path("/vres")
  @Produces({ MediaType.APPLICATION_JSON })
  @Deprecated
  public Set<String> getAvailableVREs() {
    return vreManager.getAvailableVREIds();
  }

  @POST
  @APIDesc("Searches the Solr index")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response post(SearchParameters searchParams, @HeaderParam("VRE_ID") String vreId) {
    VRE vre = Strings.isNullOrEmpty(vreId) ? vreManager.getDefaultVRE() : vreManager.getVREById(vreId);
    if (vre == null) {
      throw new TimbuctooException(Response.Status.NOT_FOUND, "No such VRE: %s", vreId);
    }

    Scope scope = vre.getScope();

    String typeString = searchParams.getTypeString();
    if (Strings.isNullOrEmpty(typeString)) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "No typeString specified");
    }
    Class<? extends Entity> type = registry.getTypeForIName(typeString);
    if (type == null) {
      throw new TimbuctooException(Response.Status.NOT_FOUND, "No such type: %s", typeString);
    }
    if (!TypeRegistry.isDomainEntity(type)) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "Not a domain entity type: %s", typeString);
    }

    if (!scope.isTypeInScope(TypeRegistry.toDomainEntity(type))) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "Type not in scope: %s", typeString);
    }

    String q = searchParams.getTerm();
    if (Strings.isNullOrEmpty(q)) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "No 'q' parameter specified");
    }

    // Process
    try {
      SearchResult result = searchManager.search(scope, TypeRegistry.toDomainEntity(type), searchParams);
      storageManager.addSystemEntity(SearchResult.class, result);
      String queryId = result.getId();
      return Response.created(new URI(queryId)).build();
    } catch (NoSuchFacetException e) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "No such facet: %s", e.getMessage());
    } catch (Exception e) {
      throw new TimbuctooException(Response.Status.INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
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
      @Context UriInfo uriInfo//
  ) {

    // Retrieve result
    SearchResult result = storageManager.getEntity(SearchResult.class, queryId);
    if (result == null) {
      throw new TimbuctooException(Response.Status.NOT_FOUND, "Missing entity: SearchResult %s",  queryId);
    }

    // Process
    Class<? extends Entity> entityType = registry.getTypeForIName(result.getSearchType());
    if (entityType == null) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "No entity type for %s",  result.getSearchType());
    }
    if (!TypeRegistry.isDomainEntity(entityType)) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "Not a domain entity type: %s", entityType);
    }
    Class<? extends DomainEntity> type = TypeRegistry.toDomainEntity(entityType);

    List<String> ids = result.getIds() != null ? result.getIds() : Lists.<String> newArrayList();
    int idsSize = ids.size();
    int lo = toRange(start, 0, idsSize);
    int hi = toRange(lo + rows, 0, idsSize);
    List<String> idsToGet = ids.subList(lo, hi);

    // Retrieve entities one-by-one to retain ordering
    List<DomainEntity> entities = Lists.newArrayList();
    for (String id : idsToGet) {
      DomainEntity entity = storageManager.getEntity(type, id);
      if (entity != null) {
        entities.add(entity);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }

    Map<String, Object> returnValue = Maps.newHashMap();
    returnValue.put("term", result.getTerm());
    returnValue.put("facets", result.getFacets());
    returnValue.put("numFound", idsSize);
    returnValue.put("ids", idsToGet);
    returnValue.put("refs", createEntityRefs(type, entities));
    returnValue.put("results", entities);
    returnValue.put("start", lo);
    returnValue.put("rows", idsToGet.size());
    returnValue.put("sortableFields", searchManager.findSortableFields(type));

    LOG.debug("path: {}", uriInfo.getAbsolutePath());

    if (start > 0) {
      int prevStart = Math.max(start - rows, 0);
      URI prev = createHATEOASURI(prevStart, rows, uriInfo, queryId);
      returnValue.put("_prev", prev);
    }

    if (hi < idsSize) {
      URI next = createHATEOASURI(start + rows, rows, uriInfo, queryId);
      returnValue.put("_next", next);
    }

    return Response.ok(returnValue).build();
  }

  private URI createHATEOASURI(final int start, final int rows, UriInfo uriInfo, String queryId) {
    UriBuilder builder = UriBuilder.fromUri(config.getSetting("public_url"));
    builder.path("search");
    builder.path(queryId);
    builder.queryParam("start", start).queryParam("rows", rows);
    return builder.build();
  }

  private int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private List<EntityRef> createEntityRefs(Class<? extends DomainEntity> type, List<DomainEntity> entities) {
    String itype = registry.getINameForType(type);
    String xtype = registry.getXNameForType(type);
    List<EntityRef> list = Lists.newArrayListWithCapacity(entities.size());
    for (DomainEntity entity : entities) {
      list.add(new EntityRef(itype, xtype, entity.getId(), entity.getDisplayName()));
    }
    return list;
  }

  public static class EntityRef {
    private final String type;
    private final String id;
    private final String path;
    private final String displayName;

    public EntityRef(String type, String xtype, String id, String displayName) {
      this.type = type;
      this.id = id;
      this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
      this.displayName = displayName;
    }

    public String getType() {
      return type;
    }

    public String getId() {
      return id;
    }

    public String getPath() {
      return path;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

}
