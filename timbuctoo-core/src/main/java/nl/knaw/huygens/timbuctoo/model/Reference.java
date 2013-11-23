package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.config.TypeNames;

/**
 * A reference to an {@code Entity} instance,
 * allowing it to be retrieved from the storage.
 */
public class Reference {

  /** The internal type name. */
  private String type;
  /** The identifier. */
  private String id;

  // For deserialization...
  public Reference() {}

  public Reference(Class<? extends Entity> typeToken, String id) {
    this.type = TypeNames.getInternalName(typeToken);
    this.id = id;
  }

  public Reference(String type, String id) {
    this.type = type;
    this.id = id;
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

  @Override
  public boolean equals(Object object) {
    if (object instanceof Reference) {
      Reference that = (Reference) object;
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
