package nl.knaw.huygens.security;

import java.security.Principal;

/**
 * A class that contains the mandatory information, that is needed to create a SecurityContext.
 *
 */
public class SecurityInformation {
  private String applicationName;
  private String displayName;
  private Principal principal;

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Principal getPrincipal() {
    return principal;
  }

  public void setPrincipal(Principal principal) {
    this.principal = principal;
  }

}
