package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

@IDPrefix("KEYW")
public class Keyword extends DomainEntity {

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
