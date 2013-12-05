package nl.knaw.huygens.timbuctoo.config;

/**
 * Definitions of some resource paths.
 */
public interface Paths {

  /** Used for system entities. */
  public static final String SYSTEM_PREFIX = "system";
  /** Used for domain entities. */
  public static final String DOMAIN_PREFIX = "domain";
  String ENTITY_REGEX = "[a-zA-Z]+";
  String ID_REGEX = "[a-zA-Z]{4}\\d+";

}
