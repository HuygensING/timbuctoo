package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix(Login.ID_PREFIX)
public class Login extends SystemEntity {

  public static final String ID_PREFIX = "LOGI";
  private String userPid;
  private String authString;

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setUserPid(String userPid) {
    this.userPid = userPid;
  }

  public String getUserPid() {
    return userPid;
  }

  public String getAuthString() {
    return authString;
  }

  public void setAuthString(String authString) {
    this.authString = authString;
  }

}
