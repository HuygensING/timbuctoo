package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.facet.IndexAnnotation;

@IDPrefix("KEYW")
public class Keyword extends DomainDocument {

  private String type;
  private String value;

  @Override
  public String getDisplayName() {
    return value;
  };

  @IndexAnnotation(fieldName = "dynamic_s_type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @IndexAnnotation(fieldName = "dynamic_t_value", isFaceted = false)
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
