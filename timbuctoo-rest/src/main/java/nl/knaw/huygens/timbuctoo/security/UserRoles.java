package nl.knaw.huygens.timbuctoo.security;

/**
 * A helper class that defines the possible user roles.
 */
public class UserRoles {
  public static final String ADMIN_ROLE = "ADMIN";
  public static final String UNVERIFIED_USER_ROLE = "UNVERIFIED_USER";
  public static final String USER_ROLE = "USER";

  private UserRoles() {
    throw new AssertionError("Non-instantiable class");
  }

}
