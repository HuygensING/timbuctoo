package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class PropertyTripleProcessor {
  private final RdfImportSession rdfImportSession;

  public PropertyTripleProcessor(RdfImportSession rdfImportSession) {
    this.rdfImportSession = rdfImportSession;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Node subject = triple.getSubject();
    String entityUri = subject.isBlank() ? subject.getBlankNodeLabel() : subject.getURI();
    RdfProperty property = new RdfProperty(
      triple.getPredicate().getURI(),
      triple.getObject().getLiteralLexicalForm(),
      triple.getObject().getLiteralDatatypeURI()
    );
    if (isAssertion) {
      // FIXME Add support for multiple value types
      rdfImportSession.assertProperty(entityUri, property);
    } else {
      rdfImportSession.retractProperty(entityUri, property);
    }
  }

}
