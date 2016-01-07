package nl.knaw.huygens.timbuctoo.server.rest;

public class User {
  private String displayName;

  public User() {
  }

  public User(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
