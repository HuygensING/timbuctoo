package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;

import java.util.Optional;

class AddToCollectionTripleProcessor implements TripleProcessor {
  private final Database database;

  public AddToCollectionTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(String vreName, Triple triple) {
    Entity entity = database.findOrCreateEntity(vreName, triple.getSubject());

    Collection collection = database.findOrCreateCollection(vreName, triple.getObject());
    entity.addToCollection(collection);
    Optional<Collection> archetype = collection.getArchetype();
    archetype.ifPresent(entity::addToCollection);

    Collection defaultCollection = database.getDefaultCollection(vreName);
    entity.removeFromCollection(defaultCollection);
  }
}
