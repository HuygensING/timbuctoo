package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.Optional;
import java.util.Set;

class ArchetypeTripleProcessor {
  private final Database database;

  public ArchetypeTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Collection collection = database.findOrCreateCollection(vreName, triple.getSubject());
    Collection previousArchetype = collection.getArchetype().get(); // collection must have an archetype
    Node tripleObject = triple.getObject();
    Optional<Collection> archetypeCollectionOptional = database.findArchetypeCollection(tripleObject.getLocalName());

    if (!archetypeCollectionOptional.isPresent()) {
      return;
    }

    Collection archetypeCollection;
    if (isAssertion) {
      archetypeCollection = archetypeCollectionOptional.get();
    } else {
      //FIXME: assert that triple's archetype is equal to current archetype
      archetypeCollection = database.getConcepts();
    }

    collection.setArchetype(archetypeCollection, tripleObject.getURI());

    Set<Entity> entities = database.findEntitiesByCollection(collection);
    entities.forEach(entity -> {
      entity.addToCollection(archetypeCollection);
      entity.removeFromCollection(previousArchetype);
    });
  }
}
