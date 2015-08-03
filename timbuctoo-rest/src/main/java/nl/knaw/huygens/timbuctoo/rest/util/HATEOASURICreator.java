package nl.knaw.huygens.timbuctoo.rest.util;

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
import nl.knaw.huygens.timbuctoo.config.Configuration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;

public class HATEOASURICreator {

  public static final String PUBLIC_URL = "public_url";
  private final String publicUrl;

  @Inject
  public HATEOASURICreator(Configuration config) {
    publicUrl = config.getSetting(PUBLIC_URL);
  }

  /**
   * Creates a uri for the search resource.
   */
  private URI createHATEOASURI(int start, int rows, String queryId, String versionPath) {
    UriBuilder builder = UriBuilder.fromUri(publicUrl);
    if (versionPath != null) {
      builder.path(versionPath);
    }
    builder.path(SEARCH_PATH);

    builder.path(queryId);
    builder.queryParam("start", start).queryParam("rows", rows);
    return builder.build();
  }

  /**
   * Convenience method for {@code createHATEOASURI}
   */
  public String createHATEOASURIAsString(final int start, final int rows, final String queryId) {
    return createHATEOASURI(start, rows, queryId, null).toString();
  }

  public String createHATEOASURIAsString(int start, int rows, String queryId, String versionPath) {
    return createHATEOASURI(start,rows, queryId, versionPath).toString();
  }

  public String createNextResultsAsString(int currentStart, int requestedRows, int totalFound, String queryId) {
    int nextStart = currentStart + requestedRows;
    if (nextStart >= totalFound) {
      return null;
    }

    return createHATEOASURIAsString(nextStart, requestedRows, queryId);
  }

  public String createPrevResultsAsString(int currenStart, int requestedRows, String queryId) {
    int previousStart = Math.max(currenStart - requestedRows, 0);
    return createHATEOASURIAsString(previousStart, requestedRows, queryId);
  }


}
