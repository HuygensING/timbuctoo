package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

import com.google.common.base.Objects;

@IDPrefix("VREA")
public class VREAuthorization extends SystemEntity {
  private String userId;
  private String vreId;
  private List<String> roles;

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
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

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VREAuthorization)) {
      return false;
    }

    VREAuthorization other = (VREAuthorization) obj;

    return Objects.equal(other.vreId, vreId) && Objects.equal(other.userId, userId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(vreId, userId);
  }
}
