package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class AddPropertyTripleProcessor implements TripleProcessor {
  private final Database database;

  public AddPropertyTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(String vreName, Triple triple) {
    Node node = triple.getSubject();
    Entity entity = database.findOrCreateEntity(vreName, node);

    String propertyName = triple.getPredicate().getLocalName();
    String value = triple.getObject().getLiteralLexicalForm();
    entity.addProperty(propertyName, value);
  }

}
