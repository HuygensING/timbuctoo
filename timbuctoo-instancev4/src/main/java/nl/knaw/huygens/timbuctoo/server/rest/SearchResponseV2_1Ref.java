package nl.knaw.huygens.timbuctoo.server.rest;

class SearchResponseV2_1Ref {

  private final String type;
  private final String id;
  private final String path;
  private final String displayName;
  private final Object data;

  public SearchResponseV2_1Ref(String id, String type, String path, String displayName, Object data) {

    this.id = id;
    this.type = type;
    this.path = path;
    this.displayName = displayName;
    this.data = data;
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

  public Object getData() {
    return data;
  }
}
