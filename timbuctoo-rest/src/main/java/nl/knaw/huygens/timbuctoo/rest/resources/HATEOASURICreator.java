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

import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V1_PATH;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.timbuctoo.config.Configuration;

import com.google.inject.Inject;

public class HATEOASURICreator {
  private Configuration config;

  @Inject
  public HATEOASURICreator(Configuration config) {
    this.config = config;
  }

  /**
   * Creates a uri for the search resource.
   * @param start
   * @param rows
   * @param queryId
   * @return
   */
  public URI createHATEOASURI(final int start, final int rows, final String queryId) {

    UriBuilder builder = UriBuilder.fromUri(config.getSetting("public_url"));
    builder.path(V1_PATH);
    builder.path(SEARCH_PATH);

    builder.path(queryId);
    builder.queryParam("start", start).queryParam("rows", rows);
    return builder.build();
  }

  /**
   * Convenience method for {@code createHATEOASURI}
   * @param start
   * @param rows
   * @param queryId
   * @return
   */
  public String createHATEOASURIAsString(final int start, final int rows, final String queryId) {
    return createHATEOASURI(start, rows, queryId).toString();
  }
}
