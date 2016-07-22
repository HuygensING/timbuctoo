package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.Set;

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

    Set<Entity> entities = database.findEntitiesByCollection(collection);
    entities.forEach(entity -> entity.addToCollection(archetypeCollection));
  }
}
