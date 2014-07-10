package nl.knaw.huygens.timbuctoo.rest.resources;

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
