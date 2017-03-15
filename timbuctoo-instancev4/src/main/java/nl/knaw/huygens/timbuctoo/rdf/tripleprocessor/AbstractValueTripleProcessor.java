package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

public abstract class AbstractValueTripleProcessor {
  public final void process(
    String vreName,
    String subject,
    String predicate,
    String lexicalValue,
    String typeUri,
    boolean isAssertion
  ) {
    if (isAssertion) {
      processAssertion(vreName, subject, predicate, lexicalValue, typeUri);
    } else {
      processRetraction(vreName, subject, predicate, lexicalValue, typeUri);
    }
  }

  protected abstract void processAssertion(String vreName, String subject, String predicate,
                                           String lexicalValue, String typeUri);

  protected abstract void processRetraction(String vreName, String subject, String predicate,
                                            String lexicalValue, String typeUri);
}
