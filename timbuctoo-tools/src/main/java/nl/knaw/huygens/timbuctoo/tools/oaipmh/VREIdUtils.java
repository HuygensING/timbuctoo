package nl.knaw.huygens.timbuctoo.tools.oaipmh;

public class VREIdUtils {
  public static String simplifyVREId(String vreId) {
    return removeNonWordCharacters(vreId).toLowerCase();
  }

  private static String removeNonWordCharacters(String vreId) {
    return vreId.replaceAll("[\\W]|_", "");
  }
}
