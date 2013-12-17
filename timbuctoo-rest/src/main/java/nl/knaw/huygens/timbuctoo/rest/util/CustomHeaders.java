package nl.knaw.huygens.timbuctoo.rest.util;

/**
 * An helper class which contains all the custom headers used in timbuctoo.
 *
 */
public class CustomHeaders {

  public static final String USER_ID_KEY = "USER_ID";
  public static final String VRE_ID_KEY = "VRE_ID";
  public static final String VRE_KEY = "VRE";

  private CustomHeaders() {
    throw new AssertionError("Non-instantiable class");
  }
}
