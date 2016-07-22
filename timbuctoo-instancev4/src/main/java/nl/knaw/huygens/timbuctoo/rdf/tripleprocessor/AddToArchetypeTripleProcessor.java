package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class AddToArchetypeTripleProcessor implements TripleProcessor {
  private final Database database;

  public AddToArchetypeTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(Triple triple, String vreName) { // TODO flip parameters
    Collection collection = database.findOrCreateCollection(vreName, triple.getSubject());
    Node tripleObject = triple.getObject();
    Collection archetypeCollection = database.findOrCreateCollection(vreName, tripleObject);

    collection.setArchetype(archetypeCollection, tripleObject.getURI());
  }
}
