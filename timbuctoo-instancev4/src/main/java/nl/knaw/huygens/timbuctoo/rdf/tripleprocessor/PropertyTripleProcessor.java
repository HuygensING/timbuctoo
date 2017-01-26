package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import org.apache.jena.graph.impl.LiteralLabel;

class PropertyTripleProcessor extends AbstractValueTripleProcessor {
  private final RdfImportSession rdfImportSession;

  public PropertyTripleProcessor(RdfImportSession rdfImportSession) {
    this.rdfImportSession = rdfImportSession;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, LiteralLabel object) {
    RdfProperty property = new RdfProperty(predicate, object.getLexicalForm(), object.getDatatypeURI());

    // FIXME Add support for multiple value types
    rdfImportSession.assertProperty(subject, property);
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, LiteralLabel object) {
    RdfProperty property = new RdfProperty(predicate, object.getLexicalForm(), object.getDatatypeURI());
    rdfImportSession.retractProperty(subject, property);
  }

}
