package nl.knaw.huygens.timbuctoo.core.dto.rdf;

public class RdfReadProperty {
  private String predicate;
  private String value;

  public RdfReadProperty(String predicate, String value) {
    this.predicate = predicate;
    this.value = value;
  }

  public String getPredicate() {
    return predicate;
  }

  public String getValue() {
    return value;
  }
}
