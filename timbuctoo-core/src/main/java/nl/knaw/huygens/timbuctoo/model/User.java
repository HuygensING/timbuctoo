package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

@IDPrefix(User.ID_PREFIX)
public class User extends SystemEntity {

  // Unique definition of prefix; also used in UserResource
  public static final String ID_PREFIX = "USER";

  private String userId; // a unique id to identify the use.
  private String vreId; // the name of the VRE.
  public String email;
  public String firstName;
  public String lastName;
  public String displayName;
  public List<String> groups;
  private List<String> roles;

  @Override
  @JsonIgnore
  public String getDisplayName() {
    return firstName + " " + lastName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vREId) {
    this.vreId = vREId;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User)) {
      return false;
    }

    User other = (User) obj;

    return Objects.equal(other.userId, userId);

  }

  @Override
  public int hashCode() {
    //Google Objects
    return Objects.hashCode(userId);
  }
}
