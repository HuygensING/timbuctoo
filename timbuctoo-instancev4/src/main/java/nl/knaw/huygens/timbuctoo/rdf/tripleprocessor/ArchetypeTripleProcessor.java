package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;

import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.RdfNameHelper.getEntityTypeName;

class ArchetypeTripleProcessor extends AbstractReferenceTripleProcessor {
  private final Database database;

  public ArchetypeTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String object) {
    Collection collection = database.findOrCreateCollection(vreName, subject, getEntityTypeName(subject));
    Collection previousArchetype = collection.getArchetype().get(); // collection must have an archetype
    Optional<Collection> archetypeCollectionOptional = database.findArchetypeCollection(getEntityTypeName(object));

    if (!archetypeCollectionOptional.isPresent()) {
      return;
    }

    Collection archetypeCollection = archetypeCollectionOptional.get();
    collection.setArchetype(archetypeCollection, object);

    Set<Entity> entities = database.findEntitiesByCollection(collection);
    entities.forEach(entity -> entity.moveToOtherArchetype(previousArchetype, archetypeCollection));
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    Collection collection = database.findOrCreateCollection(vreName, subject, getEntityTypeName(subject));
    Collection previousArchetype = collection.getArchetype().get(); // collection must have an archetype
    Optional<Collection> archetypeCollectionOptional = database.findArchetypeCollection(getEntityTypeName(object));

    if (!archetypeCollectionOptional.isPresent()) {
      return;
    }

    //FIXME: assert that triple's archetype is equal to current archetype
    Collection defaultArchetype = database.getConcepts();

    // The concepts archetype does not have a URI, so we use an empty string of the original uri of the archetype
    collection.setArchetype(defaultArchetype, "");

    Set<Entity> entities = database.findEntitiesByCollection(collection);
    entities.forEach(entity -> entity.moveToOtherArchetype(previousArchetype, defaultArchetype));
  }
}
