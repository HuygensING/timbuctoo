package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class AltLabelTripleProcessor implements TripleProcessor {
  private final Database database;

  AltLabelTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(String vreName, boolean isAssertion, Triple triple) {
    Node node = triple.getSubject();
    Entity entity = database.findOrCreateEntity(vreName, node);

    String propertyName = triple.getPredicate().getLocalName();
    if (isAssertion) {
      String value = triple.getObject().getLiteralLexicalForm();
      entity.addToListProperty(propertyName, value);
    } else {
      entity.removeProperty(propertyName);
    }
  }
}
