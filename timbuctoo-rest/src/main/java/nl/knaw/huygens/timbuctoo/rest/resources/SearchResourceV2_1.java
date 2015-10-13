package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.inject.Inject;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.providers.CSVProvider;
import nl.knaw.huygens.timbuctoo.rest.providers.XLSProvider;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchResultMapper;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
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
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.VERSION_PARAM;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;

@Path("{" + VERSION_PARAM + " : " + V2_1_PATH + "}" + SEARCH_PATH)
public class SearchResourceV2_1 extends ResourceBase {
  private static final Logger LOG = LoggerFactory.getLogger(SearchResourceV2_1.class);

  private final TypeRegistry registry;
  private final Repository repository;
  private final Configuration config;
  private final SearchRequestValidator searchRequestValidator;
  private final RelationSearcher relationSearcher;
  private final IndexRegularSearchResultMapper regularSearchResultMapper;
  private final IndexRelationSearchResultMapper relationSearchResultMapper;
  private final VRECollection vreCollection;
  private final RelationSearchParametersConverter relationSearchParametersConverter;

  @Inject
  public SearchResourceV2_1(TypeRegistry registry, Repository repository, Configuration config, SearchRequestValidator searchRequestValidator, RelationSearcher relationSearcher, IndexRegularSearchResultMapper regularSearchResultMapper, IndexRelationSearchResultMapper relationSearchResultMapper, VRECollection vreCollection, RelationSearchParametersConverter relationSearchParametersConverter) {
    super(repository, vreCollection);
    this.registry = registry;
    this.repository = repository;
    this.config = config;
    this.searchRequestValidator = searchRequestValidator;
    this.relationSearcher = relationSearcher;
    this.regularSearchResultMapper = regularSearchResultMapper;
    this.relationSearchResultMapper = relationSearchResultMapper;
    this.vreCollection = vreCollection;
    this.relationSearchParametersConverter = relationSearchParametersConverter;
  }

  @POST
  @Path("/" + ENTITY_PATH)
  @APIDesc("Searches the Solr execute. Expects a search parameters body.")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response regularPost( //
                               @PathParam(VERSION_PARAM) String version, //
                               @HeaderParam(VRE_ID_KEY) String vreId, //
                               @PathParam(ENTITY_PARAM) String typeString, //
                               SearchParametersV1 searchParams //
  ) {

    searchRequestValidator.validate(vreId, typeString, searchParams);

    VRE vre = getValidVRE(vreId);
    Class<? extends DomainEntity> type = registry.getTypeForXName(typeString);

    if (Relation.class.isAssignableFrom(type)) {
      // This resource is not available for relations.
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Process
    try {
      SearchResult result = vre.search(type, searchParams);
      String queryId = saveSearchResult(result);
      return Response.created(createHATEOASURI(queryId, version)).build();
    } catch (SearchValidationException e) {
      throw new TimbuctooException(BAD_REQUEST, "Search request not valid: %s", e.getMessage());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new TimbuctooException(INTERNAL_SERVER_ERROR, "Exception: %s", e.getMessage());
    }
  }

  @APIDesc("Searches the Solr execute. Expects a relation search parameters body.")
  @POST
  @Path("/" + Paths.RELATION_SEARCH_PREFIX + "/" + Paths.ENTITY_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response relationPost( //
                                @PathParam(VERSION_PARAM) String version, //
                                @HeaderParam(VRE_ID_KEY) String vreId, //
                                @PathParam(Paths.RELATION_PARAM) String relationTypeString, //
                                @PathParam(Paths.ENTITY_PARAM) String relatedTypeParam, // the type one the relations should connect with.
                                RelationSearchParametersV2_1 params //
  ) {

    Class<? extends Relation> relationType = (Class<? extends Relation>) registry.getTypeForXName(relationTypeString);
    RelationSearchParameters relationParameters = relationSearchParametersConverter.fromRelationParametersV2_1(params);
    searchRequestValidator.validateRelationRequest(vreId, relationTypeString, relationParameters);
    VRE vre = getValidVRE(vreId);

    String queryId = null;
    try {
      queryId = vre.searchRelations(relationType, relationParameters);
    } catch (SearchValidationException e) {
      return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
    } catch (StorageException e) {
      LOG.error("Could not store the search result.", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Storage went wrong.").build();
    } catch (SearchException e) {
      LOG.error("Search went wrong", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Search went wrong.").build();
    }

    return Response.created(createHATEOASURI(queryId, version)).build();
  }

  @GET
  @Path("/{id: " + Paths.ID_REGEX + "}")
  @Produces({MediaType.APPLICATION_JSON})
  @APIDesc("Returns (paged) search results Query params: \"start\" (default: 0) \"rows\" (default: 10)")
  public Response get( //
                       @PathParam("id") String queryId, //
                       @QueryParam("start") @DefaultValue("0") final int start, //
                       @QueryParam("rows") @DefaultValue("10") final int rows, //
                       @PathParam(VERSION_PARAM) String version) {

    // Retrieve result
    SearchResult result = getSearchResult(queryId);
    checkNotNull(result, NOT_FOUND, "No SearchResult with id %s", queryId);

    // Process
    String typeString = result.getSearchType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for %s", typeString);

    SearchResultDTO dto = getSearchResultMapper(type).create(type, result, start, rows, version);
    return Response.ok(dto).build();
  }

  @GET
  @Path("/{id: " + SearchResult.ID_PREFIX + Paths.ID_REGEX + "}/csv")
  @Produces({CSVProvider.TEXT_CSV})
  public Response getRelationSearchResultAsCSV(@PathParam("id") String queryId, @PathParam(VERSION_PARAM) String version) {
    SearchResult result = getSearchResult(queryId);
    checkNotNull(result, NOT_FOUND, "No SearchResult with id %s", queryId);

    String typeString = result.getSearchType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for %s", typeString);
    checkCondition(Relation.class.isAssignableFrom(type), BAD_REQUEST, "Not a relation type: %s", typeString);

    RelationSearchResultDTO dto = relationSearchResultMapper.create(type, result, 0, Integer.MAX_VALUE, version);
    return Response.ok(dto) //
      .header("Content-Disposition", "attachment; filename=" + queryId + ".csv") //
      .build();
  }

  @APIDesc("Exports a search result to an Excel format.")
  @GET
  @Path("/{id: " + SearchResult.ID_PREFIX + Paths.ID_REGEX + "}/xls")
  @Produces({XLSProvider.EXCEL_TYPE_STRING})
  public Response getRelationSearchResultAsXLS(@PathParam("id") String queryId, @PathParam(VERSION_PARAM) String version) {
    SearchResult result = getSearchResult(queryId);
    checkNotNull(result, NOT_FOUND, "No SearchResult with id %s", queryId);

    String typeString = result.getSearchType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for %s", typeString);
    checkCondition(Relation.class.isAssignableFrom(type), BAD_REQUEST, "Not a relation type: %s", typeString);

    RelationSearchResultDTO dto = relationSearchResultMapper.create(type, result, 0, Integer.MAX_VALUE, version);
    return Response.ok(dto) //
      .header("Content-Disposition", "attachment; filename=" + queryId + ".xls") //
      .build();
  }

  private SearchResultMapper getSearchResultMapper(Class<? extends DomainEntity> type) {
    return Relation.class.isAssignableFrom(type) ? relationSearchResultMapper : regularSearchResultMapper;
  }

  private URI createHATEOASURI(String queryId, String apiVersion) {
    UriBuilder builder = UriBuilder.fromUri(config.getSetting("public_url"));
    builder.path(apiVersion);
    builder.path(SEARCH_PATH);
    builder.path(queryId);
    return builder.build();
  }

  private SearchResult getSearchResult(String id) {
    return repository.getEntityOrDefaultVariation(SearchResult.class, id);
  }

  private String saveSearchResult(SearchResult result) throws StorageException, ValidationException {
    return repository.addSystemEntity(SearchResult.class, result);
  }

}
