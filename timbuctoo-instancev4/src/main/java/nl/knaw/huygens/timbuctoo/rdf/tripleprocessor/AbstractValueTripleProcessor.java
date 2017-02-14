package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.impl.LiteralLabel;

public abstract class AbstractValueTripleProcessor {
  public final void process(
    String vreName,
    String subject,
    String predicate,
    LiteralLabel object,
    boolean isAssertion
  ) {
    if (isAssertion) {
      processAssertion(vreName, subject, predicate, object);
    } else {
      processRetraction(vreName, subject, predicate, object);
    }
  }

  protected abstract void processAssertion(String vreName, String subject, String predicate, LiteralLabel object);

  protected abstract void processRetraction(String vreName, String subject, String predicate, LiteralLabel object);
}
