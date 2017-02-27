package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.commons.lang.builder.ToStringBuilder;

public class LiteralTriple implements Triple {
  private final String subject;
  private final String predicate;
  private final String object;
  private final String datatype;

  public LiteralTriple(String subject, String predicate, String object, String datatype) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
    this.datatype = datatype;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public String getPredicate() {
    return predicate;
  }

  public String getObject() {
    return object;
  }

  public String getDatatype() {
    return datatype;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
