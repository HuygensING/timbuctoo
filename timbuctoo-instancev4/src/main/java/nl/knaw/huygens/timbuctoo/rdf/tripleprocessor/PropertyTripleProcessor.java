package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Triple;

class PropertyTripleProcessor {
  private final Database database;
  private final RdfImportSession rdfImportSession;

  public PropertyTripleProcessor(Database database, RdfImportSession rdfImportSession) {
    this.database = database;
    this.rdfImportSession = rdfImportSession;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    String entityUri = triple.getSubject().getURI();
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
