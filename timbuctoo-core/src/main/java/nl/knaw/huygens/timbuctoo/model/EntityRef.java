package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.config.Paths;

import com.google.common.base.Joiner;

/**
 * A reference to an entity, to be used in other entities.
 * The reference is partially denormalized by including the display name.
 *
 * It is an open question whether we should include the variation.
 */
public class EntityRef {

  private String type;
  private String id;
  private String path;
  private String displayName;

  // For deserialization...
  public EntityRef() {}

  public EntityRef(String type, String xtype, String id, String displayName) {
    this.type = type;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    this.displayName = displayName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof EntityRef) {
      EntityRef that = (EntityRef) object;
      return (this.type == null ? that.type == null : this.type.equals(that.type)) //
          && (this.id == null ? that.id == null : this.id.equals(that.id));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (type == null ? 0 : type.hashCode());
    result = 31 * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return String.format("{%s,%s}", type, id);
  }

}
