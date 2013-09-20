package nl.knaw.huygens.repository.model;

import org.apache.commons.lang.ObjectUtils;

/**
 * A class that represents a link to other objects.
 * @author martijnm
 */
public class Reference {

  private String type;
  private String id;

  // Default constructor for deserializing
  public Reference() {}

  public Reference(Class<? extends Document> typeToken, String id) {
    this.type = typeToken.getSimpleName().toLowerCase();
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
  public boolean equals(Object obj) {
    // a null reference will never be an instance of Reference.
    if (obj instanceof Reference) {
      Reference other = (Reference) obj;
      boolean isEqual = ObjectUtils.equals(id, other.id);
      isEqual &= ObjectUtils.equals(type, other.type);
      return isEqual;
    }
    return false;
  }

}
