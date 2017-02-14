package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

/**
 * Class that helps to parse the parts of a triple
 */
public class TripleParser {
  private final Triple triple;

  public TripleParser(Triple triple) {
    this.triple = triple;

  }

  public static TripleParser fromTriple(Triple triple) {
    return new TripleParser(triple);
  }

  public String getSubjectReference() {
    Node subject = triple.getSubject();
    if (subject.isURI()) {
      return subject.getURI();
    } else {
      return subject.getBlankNodeLabel();
    }
  }

  public String getPredicateReference() {
    return triple.getPredicate().getURI();
  }

  public String getObjectReference() {
    Node object = triple.getObject();
    if (object.isLiteral()) {
      throw new IllegalArgumentException(object + " is a literal, not a reference.");
    }
    if (object.isURI()) {
      return object.getURI();
    } else {
      return object.getBlankNodeLabel();
    }
  }

  public LiteralLabel getObjectAsLiteral() {
    Node object = triple.getObject();
    if (!object.isLiteral()) {
      throw new IllegalArgumentException(object + " is NOT a literal.");
    }
    return object.getLiteral();
  }
}
