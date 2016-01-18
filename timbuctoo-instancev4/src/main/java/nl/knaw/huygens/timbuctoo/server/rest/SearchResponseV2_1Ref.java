package nl.knaw.huygens.timbuctoo.server.rest;

class SearchResponseV2_1Ref {

  private final String type;
  private final String id;
  private final String path;
  private final String displayName;

  public SearchResponseV2_1Ref(String id, String type, String path, String displayName) {

    this.id = id;
    this.type = type;
    this.path = path;
    this.displayName = displayName;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public String getDisplayName() {
    return displayName;
  }
}
