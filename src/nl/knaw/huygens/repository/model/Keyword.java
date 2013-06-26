package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;

@IDPrefix("KEY")
@DocumentTypeName("keyword")
public class Keyword extends DomainDocument {

  public String type;
  public String value;

  @Override
  public String getDescription() {
    return value;
  };

  @IndexAnnotation(fieldName = "facet_s_type")
  public String getType() {
    return type;
  }

  @IndexAnnotation(fieldName = "facet_t_value")
  public String getValue() {
    return value;
  }

}
