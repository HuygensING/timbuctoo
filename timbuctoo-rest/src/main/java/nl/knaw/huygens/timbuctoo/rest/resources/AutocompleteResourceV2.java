package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.util.AutocompleteResultConverter;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.config.Paths.AUTOCOMPLETE_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.DOMAIN_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.QUERY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.ROWS;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.START;

@Path(V2_PATH + "/" + DOMAIN_PREFIX + "/" + ENTITY_PATH + "/" + AUTOCOMPLETE_PATH)
public class AutocompleteResourceV2 extends ResourceBase {
  private static final int NOT_IMPLEMENTED = 501;
  private static final String NO_AUTOCOMPLETE = "VRE with id %s does not support autocomplete on collection %s";
  public static final String DEFAULT_ROWS = "10";
  public static final String DEFAULT_START = "0";
  private static final Logger LOG = LoggerFactory.getLogger(AutocompleteResourceV2.class);
  private final Configuration config;
  private final TypeRegistry typeRegistry;
  private final AutocompleteResultConverter resultConverter;

  @Inject
  public AutocompleteResourceV2(Configuration config, Repository repository, VRECollection vreCollection, TypeRegistry typeRegistry, AutocompleteResultConverter resultConverter) {
    super(repository, vreCollection);
    this.config = config;
    this.typeRegistry = typeRegistry;
    this.resultConverter = resultConverter;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam(ENTITY_PARAM) String entityName, @QueryParam(QUERY) @DefaultValue("*") String query, //
      @QueryParam(START) @DefaultValue(DEFAULT_START) int start, @QueryParam(ROWS) @DefaultValue(DEFAULT_ROWS) int rows, @HeaderParam(VRE_ID_KEY) String vreId) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    VRE vre = getValidVRE(vreId);

    Iterable<Map<String, Object>> rawSearchResult = null;
    try {
      rawSearchResult = vre.doRawSearch(type, query, start, rows, Maps.<String, Object>newHashMap());
    } catch (NotInScopeException e) {
      return mapException(BAD_REQUEST, e);
    } catch (SearchException e) {
      LOG.error("Search has failed.", e);

      return mapException(INTERNAL_SERVER_ERROR, e);
    } catch (RawSearchUnavailableException e) {
      return super.mapException(NOT_IMPLEMENTED, String.format(NO_AUTOCOMPLETE, vreId, entityName));
    }

    Iterable<Map<String, Object>> result = resultConverter.convert(rawSearchResult, getCollectionUri(entityName));

    return Response.ok(result).build();
  }

  private URI getCollectionUri(String entityName) {
    return UriBuilder.fromPath(config.getSetting("public_url")).path(DOMAIN_PREFIX).path(entityName).build();
  }

  protected final Class<? extends DomainEntity> getValidEntityType(String name) {
    return checkNotNull(typeRegistry.getTypeForXName(name), NOT_FOUND, "No domain entity collection %s", name);
  }
}
