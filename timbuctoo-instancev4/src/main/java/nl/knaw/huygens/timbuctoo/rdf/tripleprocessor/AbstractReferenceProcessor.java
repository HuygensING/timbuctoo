package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

/**
 * An abstract class to use when importing a triple with an object that is a "uri node" or a "blank node".
 */
public abstract class AbstractReferenceProcessor {
  public final void process(String vreName, String subject, String predicate, String object, boolean isAssertion) {
    if (isAssertion) {
      processAssertion(vreName, subject, predicate, object);
    } else {
      processRetraction(vreName, subject, predicate, object);
    }
  }

  protected abstract void processAssertion(String vreName, String subject, String predicate, String object);

  protected abstract void processRetraction(String vreName, String subject, String predicate, String object);
}
