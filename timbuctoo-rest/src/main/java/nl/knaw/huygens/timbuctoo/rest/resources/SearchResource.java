package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.search.converters.SearchParametersConverter;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.URI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;

@Path("search")
public class SearchResource extends ResourceBase {

  private static final String RELATION_SEARCH_PREFIX = "relations";

  private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

  private final TypeRegistry registry;

  private final SearchRequestValidator searchRequestValidator;
  final SearchParametersConverter searchParametersConverter;
  private final RelationSearcher relationSearcher;
  private final RegularSearchResultMapper regularSearchResultMapper;
  private final RelationSearchResultMapper relationSearchResultMapper;

  @Inject
  public SearchResource(TypeRegistry registry, Repository repository, SearchRequestValidator searchRequestValidator, SearchParametersConverter searchParametersConverter,
      RelationSearcher relationSearcher, RegularSearchResultMapper regularSearchResultMapper, RelationSearchResultMapper relationSearchResultMapper, VRECollection vreCollection) {
    super(repository, vreCollection);
    this.registry = registry;
    this.searchRequestValidator = searchRequestValidator;
    this.searchParametersConverter = searchParametersConverter;
    this.relationSearcher = relationSearcher;
    this.regularSearchResultMapper = regularSearchResultMapper;
    this.relationSearchResultMapper = relationSearchResultMapper;
  }

  @POST
  @APIDesc("Searches the Solr execute. Expects a search parameters body.")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response regularPost(SearchParameters searchParams, @HeaderParam(VRE_ID_KEY) String vreId) {

    SearchParametersV1 searchParamsV1 = searchParametersConverter.toV1(searchParams);

    String typeString = StringUtils.trimToNull(searchParams.getTypeString());
    searchRequestValidator.validate(vreId, registry.getXNameForIName(typeString), searchParamsV1);

    VRE vre = getValidVRE(vreId);
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);

    // Process
    try {
      SearchResult result = vre.search(type, searchParamsV1);
      String queryId = putSearchResult(result);
      return Response.created(new URI(queryId)).build();
    } catch (SearchValidationException e) {
      throw new TimbuctooException(BAD_REQUEST, "Search request not valid: %s", e.getMessage());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  @GET
  @Path("/{id: " + Paths.ID_REGEX + "}")
  @APIDesc("Returns (paged) search results Query params: \"start\" (default: 0) \"rows\" (default: 10)")
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

    SearchResultDTO dto = regularSearchResultMapper.create(type, result, start, rows, null);
    return Response.ok(dto).build();
  }

  private SearchResult getSearchResult(String id) {
    return repository.getEntityOrDefaultVariation(SearchResult.class, id);
  }

  private String putSearchResult(SearchResult result) throws StorageException, ValidationException {
    return repository.addSystemEntity(SearchResult.class, result);
  }

  // ---------------------------------------------------------------------------

  @POST
  @Path("/" + RELATION_SEARCH_PREFIX)
  @Consumes(MediaType.APPLICATION_JSON)
  @APIDesc("Searches the Solr execute. Expects a relation search parameters body.")
  public Response relationPost(@HeaderParam(VRE_ID_KEY) String vreId, RelationSearchParameters params) {

    final String typeString = params.getTypeString();
    Class<? extends DomainEntity> relationType = registry.getDomainEntityType(typeString);

    searchRequestValidator.validateRelationRequest(vreId, registry.getXNameForIName(typeString), params);

    VRE vre = getValidVRE(vreId);

    // Process
    try {
      SearchResult result = relationSearcher.search(vre, relationType, params);
      String queryId = putSearchResult(result);
      return Response.created(new URI(queryId)).build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  @APIDesc("Returns (paged) search results Query params: \"start\" (default: 0) \"rows\" (default: 10)")
  @GET
  @Path("/" + RELATION_SEARCH_PREFIX + "/{id: " + SearchResult.ID_PREFIX + Paths.ID_REGEX + "}")
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

    SearchResultDTO dto = relationSearchResultMapper.create(type, result, start, rows, null);
    return Response.ok(dto).build();
  }

}
