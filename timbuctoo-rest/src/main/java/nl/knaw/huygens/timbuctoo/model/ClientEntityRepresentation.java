package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.config.Paths;

import com.google.common.base.Joiner;

public class ClientEntityRepresentation {
  private final String type;
  private final String id;
  private final String path;
  private final String displayName;

  public ClientEntityRepresentation(String type, String xtype, String id, String displayName) {
    this.type = type;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
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