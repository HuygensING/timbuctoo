package nl.knaw.huygens.timbuctoo.config;

/**
 * Definitions of some resource paths.
 */
public interface Paths {

  /** Used for system entities. */
  public static final String SYSTEM_PREFIX = "system";
  /** Used for domain entities. */
  public static final String DOMAIN_PREFIX = "domain";
  /** Regex for determining the entity name.*/
  public static final String ENTITY_REGEX = "[a-zA-Z]+";
  /** Regex for determining hte id.*/
  public static final String ID_REGEX = "[a-zA-Z]{4}\\d+";
  /** The path of the {@code UserResource} */
  public static final String USER_PATH = "users";
}
