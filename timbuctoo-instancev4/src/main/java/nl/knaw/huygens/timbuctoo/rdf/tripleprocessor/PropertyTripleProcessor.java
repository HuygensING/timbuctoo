package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class PropertyTripleProcessor {
  private final Database database;

  public PropertyTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Node node = triple.getSubject();
    Entity entity = database.findOrCreateEntity(vreName, node);

    String propertyName = triple.getPredicate().getLocalName();
    if (isAssertion) {
      String value = triple.getObject().getLiteralLexicalForm();
      entity.addProperty(propertyName, value);
    } else {
      entity.removeProperty(propertyName);
    }
  }

}
