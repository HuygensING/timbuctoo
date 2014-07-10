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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V1_PATH;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.search.SearchValidationException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path(V1_PATH + "/" + SEARCH_PATH)
public class SearchResourceV1 extends ResourceBase {

  private static final String RELATION_PARAM = "relationType";
  private static final String RELATION_SEARCH_PREFIX = "{" + RELATION_PARAM + ": [a-z]*relations }";

  private static final Logger LOG = LoggerFactory.getLogger(SearchResourceV1.class);

  @Inject
  private TypeRegistry registry;
  @Inject
  private Repository repository;
  @Inject
  private SearchManager searchManager;
  @Inject
  private VREManager vreManager;
  @Inject
  private Configuration config;
  @Inject
  private SearchRequestValidator searchRequestValidator;
  @Inject
  private RelationSearcher relationSearcher;
  @Inject
  private RegularClientSearchResultCreator regularSearchResultCreator;
  @Inject
  private RelationClientSearchResultCreator relationrSearchResultCreator;

  @GET
  @Path("/vres")
  @Produces({ MediaType.APPLICATION_JSON })
  @Deprecated
  public Set<String> getAvailableVREs() {
    return vreManager.getAvailableVREIds();
  }

  @POST
  @Path("/" + ENTITY_PATH)
  @APIDesc("Searches the Solr index")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response regularPost(SearchParametersV1 searchParams, @PathParam(ENTITY_PARAM) String typeString, @HeaderParam("VRE_ID") String vreId) {

    searchRequestValidator.validate(vreId, typeString, searchParams);

    VRE vre = vreManager.getVREById(vreId);
    Class<? extends DomainEntity> type = registry.getTypeForXName(typeString);

    // Process
    try {
      SearchResult result = searchManager.search(vre, type, searchParams);
      String queryId = saveSearchResult(result);
      return Response.created(createHATEOASURI(queryId)).build();
    } catch (SearchValidationException e) {
      throw new TimbuctooException(BAD_REQUEST, "Search request not valid: %s", e.getMessage());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  @GET
  @Path("/{id: " + SearchResult.ID_PREFIX + "\\d+}")
  @APIDesc("Returns (paged) search results")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response regularGet( //
      @PathParam("id") String queryId, //
      @QueryParam("start") @DefaultValue("0") final int start, //
      @QueryParam("rows") @DefaultValue("10") final int rows) {

    // Retrieve result
    SearchResult result = getSearchResult(queryId);
    checkNotNull(result, NOT_FOUND, "No SearchResult with id %s", queryId);

    // Process
    String typeString = result.getSearchType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for %s", typeString);

    final RegularClientSearchResult clientSearchResult = getClientSearchResultCreator(type).create(type, result, start, rows);
    return Response.ok(clientSearchResult).build();
  }

  private ClientSearchResultCreator getClientSearchResultCreator(Class<? extends DomainEntity> type) {
    return Relation.class.isAssignableFrom(type) ? relationrSearchResultCreator : regularSearchResultCreator;
  }

  private URI createHATEOASURI(String queryId) {
    UriBuilder builder = UriBuilder.fromUri(config.getSetting("public_url"));
    builder.path(V1_PATH);
    builder.path(SEARCH_PATH);
    builder.path(queryId);

    return builder.build();
  }

  private URI createHATEOASURI(final int start, final int rows, String queryId, boolean isRelationSearch) {
    UriBuilder builder = UriBuilder.fromUri(config.getSetting("public_url"));
    builder.path(V1_PATH);
    builder.path(SEARCH_PATH);

    if (isRelationSearch) {
      builder.path(RELATION_SEARCH_PREFIX);
    }

    builder.path(queryId);
    builder.queryParam("start", start).queryParam("rows", rows);
    return builder.build();
  }

  private int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private SearchResult getSearchResult(String id) {
    return repository.getEntity(SearchResult.class, id);
  }

  private String saveSearchResult(SearchResult result) throws StorageException, ValidationException {
    return repository.addSystemEntity(SearchResult.class, result);
  }

  // ---------------------------------------------------------------------------

  @POST
  @Path("/" + RELATION_SEARCH_PREFIX)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response relationPost(@HeaderParam("VRE_ID") String vreId, RelationSearchParameters params, @PathParam(RELATION_PARAM) String relationTypeString) {

    Class<? extends DomainEntity> relationType = registry.getTypeForXName(relationTypeString);

    searchRequestValidator.validateRelationRequest(vreId, relationTypeString, params);

    VRE vre = vreManager.getVREById(vreId);

    // Process
    try {
      SearchResult result = relationSearcher.search(vre, relationType, params);
      String queryId = saveSearchResult(result);
      return Response.created(new URI(queryId)).build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  @GET
  @Path("/" + RELATION_SEARCH_PREFIX + "/{id: " + SearchResult.ID_PREFIX + "\\d+}")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response relationGet( //
      @PathParam("id") String queryId, //
      @QueryParam("start") @DefaultValue("0") final int start, //
      @QueryParam("rows") @DefaultValue("10") final int rows) {

    // Retrieve result
    SearchResult result = getSearchResult(queryId);
    checkNotNull(result, NOT_FOUND, "No SearchResult with id %s", queryId);

    // Process
    String typeString = result.getSearchType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for %s", typeString);
    checkCondition(Relation.class.isAssignableFrom(type), BAD_REQUEST, "Not a relation type: %s", typeString);
    @SuppressWarnings("unchecked")
    Class<? extends Relation> rtype = (Class<? extends Relation>) type;

    List<String> ids = result.getIds() != null ? result.getIds() : Lists.<String> newArrayList();
    int idsSize = ids.size();
    int lo = toRange(start, 0, idsSize);
    int hi = toRange(lo + rows, 0, idsSize);
    List<String> idsToGet = ids.subList(lo, hi);

    List<Relation> relations = retrieveRelations(rtype, idsToGet);

    Map<String, Object> returnValue = Maps.newHashMap();
    returnValue.put("numFound", idsSize);
    returnValue.put("ids", idsToGet);
    returnValue.put("refs", createRelationRefs(rtype, relations));
    returnValue.put("results", relations);
    returnValue.put("start", lo);
    returnValue.put("rows", idsToGet.size());

    if (start > 0) {
      int prevStart = Math.max(start - rows, 0);
      URI prev = createHATEOASURI(prevStart, rows, queryId, true);
      returnValue.put("_prev", prev);
    }

    if (hi < idsSize) {
      URI next = createHATEOASURI(start + rows, rows, queryId, true);
      returnValue.put("_next", next);
    }

    return Response.ok(returnValue).build();
  }

  // ---------------------------------------------------------------------------

  private List<DomainEntity> retrieveEntities(Class<? extends DomainEntity> type, List<String> ids) {
    // Retrieve one-by-one to retain ordering
    List<DomainEntity> entities = Lists.newArrayList();
    for (String id : ids) {
      DomainEntity entity = repository.getEntity(type, id);
      if (entity != null) {
        entities.add(entity);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }
    return entities;
  }

  private List<EntityRef> createEntityRefs(Class<? extends DomainEntity> type, List<DomainEntity> entities) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
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

  // ---------------------------------------------------------------------------

  private List<Relation> retrieveRelations(Class<? extends Relation> type, List<String> ids) {
    // Retrieve one-by-one to retain ordering
    List<Relation> relations = Lists.newArrayList();
    for (String id : ids) {
      Relation relation = repository.getEntity(type, id);
      if (relation != null) {
        relations.add(relation);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }
    return relations;
  }

  private List<RelationRef> createRelationRefs(Class<? extends Relation> type, List<Relation> relations) {
    EntityMappers entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    EntityMapper mapper = entityMappers.getEntityMapper(type);

    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<RelationRef> list = Lists.newArrayListWithCapacity(relations.size());
    for (Relation relation : relations) {
      RelationType relationType = repository.getRelationTypeById(relation.getTypeId());
      String relationName = relationType.getRegularName();
      DomainEntity source = retrieveEntity(mapper, relation.getSourceType(), relation.getSourceId());
      String sourceName = (source != null) ? source.getDisplayName() : "[unknown]";
      DomainEntity target = retrieveEntity(mapper, relation.getTargetType(), relation.getTargetId());
      String targetName = (target != null) ? target.getDisplayName() : "[unknown]";
      list.add(new RelationRef(itype, xtype, relation.getId(), relationName, sourceName, targetName));
    }
    return list;
  }

  private DomainEntity retrieveEntity(EntityMapper mapper, String typeName, String typeId) {
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeName);
    if (type == null) {
      LOG.error("Failed to convert {} to a domain entity", typeName);
      return null;
    }
    Class<? extends DomainEntity> mappedType = (mapper != null) ? mapper.map(type) : type;
    return repository.getEntity(mappedType, typeId);
  }

  public static class RelationRef {
    private final String type;
    private final String id;
    private final String path;
    private final String relationName;
    private final String sourceName;
    private final String targetName;

    public RelationRef(String type, String xtype, String id, String relationName, String sourceName, String targetName) {
      this.type = type;
      this.id = id;
      this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
      this.relationName = relationName;
      this.sourceName = sourceName;
      this.targetName = targetName;
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

    public String getRelationName() {
      return relationName;
    }

    public String getSourceName() {
      return sourceName;
    }

    public String getTargetName() {
      return targetName;
    }
  }

}
