package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;

import java.util.Optional;

class CollectionMembershipTripleProcessor {
  private final Database database;

  public CollectionMembershipTripleProcessor(Database database) {
    this.database = database;
  }

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
