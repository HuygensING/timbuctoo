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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Keyword;
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
import static nl.knaw.huygens.timbuctoo.config.Paths.KEYWORD_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_OR_V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.QUERY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.ROWS;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.START;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.TYPE;

@Path(V2_OR_V2_1_PATH + DOMAIN_PREFIX + "/" + KEYWORD_PATH + "/" + AUTOCOMPLETE_PATH)
public class KeywordAutocompleteResourceV2 extends ResourceBase {
  private static final int NOT_IMPLEMENTED = 501;
  private static final String NO_AUTOCOMPLETE = "VRE with id %s does not support autocomplete on collection %s";
  public static final String DEFAULT_ROWS = "10";
  public static final String DEFAULT_START = "0";
  private static final Logger LOG = LoggerFactory.getLogger(KeywordAutocompleteResourceV2.class);
  private final Configuration config;
  private final TypeRegistry typeRegistry;
  private final AutocompleteResultConverter resultConverter;

  @Inject
  public KeywordAutocompleteResourceV2(Configuration config, Repository repository, VRECollection vreCollection, TypeRegistry typeRegistry, AutocompleteResultConverter resultConverter) {
    super(repository, vreCollection);
    this.config = config;
    this.typeRegistry = typeRegistry;
    this.resultConverter = resultConverter;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam(ENTITY_PARAM) String entityName, @QueryParam(QUERY) @DefaultValue("*") String query, //
                      @QueryParam(START) @DefaultValue(DEFAULT_START) int start, @QueryParam(ROWS) @DefaultValue(DEFAULT_ROWS) int rows, //
                      @QueryParam(TYPE) String typeFilter, @HeaderParam(VRE_ID_KEY) String vreId) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    VRE vre = getValidVRE(vreId);

    Iterable<Map<String, Object>> rawSearchResult = null;
    try {
      Map<String, Object> additionalFilters = Maps.newHashMap();
      if (typeFilter != null) {
        additionalFilters.put(Keyword.INDEX_TYPE_FIELD, typeFilter);
      }

      rawSearchResult = vre.doRawSearch(type, query, start, rows, additionalFilters);
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
