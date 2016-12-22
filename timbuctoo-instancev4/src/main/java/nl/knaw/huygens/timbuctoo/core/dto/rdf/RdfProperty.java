package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class RdfProperty {
  private final String predicateUri;
  private final String value;
  private final String typeUri;

  public RdfProperty(String predicateUri, String value, String typeUri) {
    this.predicateUri = predicateUri;
    this.value = value;
    this.typeUri = typeUri;
  }


  public String getPredicateUri() {
    return predicateUri;
  }

  public String getValue() {
    return value;
  }

  public String getTypeUri() {
    return typeUri;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
