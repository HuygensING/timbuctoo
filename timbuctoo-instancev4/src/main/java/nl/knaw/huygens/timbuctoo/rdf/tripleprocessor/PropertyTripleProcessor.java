package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;

class PropertyTripleProcessor extends AbstractValueTripleProcessor {
  private final RdfImportSession rdfImportSession;

  public PropertyTripleProcessor(RdfImportSession rdfImportSession) {
    this.rdfImportSession = rdfImportSession;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate,
                                  String lexicalValue, String typeUri) {
    RdfProperty property = new RdfProperty(predicate, lexicalValue, typeUri);

    // FIXME Add support for multiple value types
    rdfImportSession.assertProperty(subject, property);
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate,
                                   String lexicalValue, String typeUri) {
    RdfProperty property = new RdfProperty(predicate, lexicalValue, typeUri);
    rdfImportSession.retractProperty(subject, property);
  }

}
