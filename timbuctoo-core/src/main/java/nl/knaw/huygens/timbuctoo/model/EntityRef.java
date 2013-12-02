package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.config.Paths;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;

/**
 * A reference to an entity, to be used in other entities.
 * The reference is partially denormalized by including the display name.
 *
 * It is an open question whether we should include the variation.
 */
public class EntityRef {

  private String itype;
  private String id;
  private String path;
  private String displayName;

  // For deserialization...
  public EntityRef() {}

  public EntityRef(String itype, String xtype, String id, String displayName) {
    this.itype = itype;
    this.id = id;
    this.path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    this.displayName = displayName;
  }

  public String getIType() {
    return itype;
  }

  public void setIType(String itype) {
    this.itype = itype;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("@path")
  public String getPath() {
    return path;
  }

  @JsonProperty("@path")
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
      return (this.itype == null ? that.itype == null : this.itype.equals(that.itype)) //
          && (this.id == null ? that.id == null : this.id.equals(that.id));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (itype == null ? 0 : itype.hashCode());
    result = 31 * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return String.format("{%s,%s}", itype, id);
  }

}
