package nl.knaw.huygens.timbuctoo.rest.util;

/**
 * Helper class to centralize important query parameters. 
 */
public class QueryParameters {

  public static final String USER_ID_KEY = "userId";
  public static final String REVISION = "rev";

  private QueryParameters() {
    throw new AssertionError("Non-instantiable class");
  }
}
