package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.config.Paths;

import com.google.common.base.Joiner;

public class ClientRelationRepresentation {
  private final String type;
  private final String id;
  private final String path;
  private final String relationName;
  private final String sourceName;
  private final String targetName;

  public ClientRelationRepresentation(String type, String xtype, String id, String relationName, String sourceName, String targetName) {
    this.type = type;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    this.relationName = relationName;
    this.sourceName = sourceName;
    this.targetName = targetName;
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

  public String getRelationName() {
    return relationName;
  }

  public String getSourceName() {
    return sourceName;
  }

  public String getTargetName() {
    return targetName;
  }
}