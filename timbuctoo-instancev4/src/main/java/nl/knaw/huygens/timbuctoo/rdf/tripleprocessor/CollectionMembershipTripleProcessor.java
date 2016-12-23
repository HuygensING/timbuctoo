package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;

class CollectionMembershipTripleProcessor {
  private final Database database;

  public CollectionMembershipTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Entity entity = database.findOrCreateEntity(vreName, triple.getSubject());

    if (isAssertion) {
      entity.addToCollection(database.findOrCreateCollection(vreName, triple.getObject()));
    } else {
      entity.removeFromCollection(database.findOrCreateCollection(vreName, triple.getObject()));
    }
  }
}
