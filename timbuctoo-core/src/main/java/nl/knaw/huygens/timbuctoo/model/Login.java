package nl.knaw.huygens.timbuctoo.model;

public class Login extends SystemEntity {

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
