package nl.knaw.huygens.repository.server.security.apis;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Copied from org.surfnet.oaaas.auth.principal.AuthenticatedPrincipal. 
 * We only use a few classes from the library and the library has a lot of dependencies, 
 * so it is better to just copy the classes we need. 
 * @author martijnm
 *
 */
public class AuthenticatedPrincipal implements Serializable, Principal {

  private static final long serialVersionUID = 1L;

  private String name;

  private Collection<String> roles;

  /*
   * Extra attributes, depending on the authentication implementation
   */
  private Map<String, Object> attributes;

  public AuthenticatedPrincipal() {
    super();
  }

  public AuthenticatedPrincipal(String username) {
    this(username, Collections.<String> emptyList());
  }

  public AuthenticatedPrincipal(String username, Collection<String> roles) {
    this(username, roles, Collections.<String, Object> emptyMap());
  }

  public AuthenticatedPrincipal(String username, Collection<String> roles, Map<String, Object> attributes) {
    this.name = username;
    this.roles = roles;
    this.attributes = attributes;
  }

  /**
   * @return the roles
   */
  public Collection<String> getRoles() {
    return roles;
  }

  /**
   * @return the attributes
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.security.Principal#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AuthenticatedPrincipalImpl [name=" + name + ", roles=" + roles + ", attributes=" + attributes + "]";
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param roles
   *          the roles to set
   */
  public void setRoles(Collection<String> roles) {
    this.roles = roles;
  }

  /**
   * @param attributes
   *          the attributes to set
   */
  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

}