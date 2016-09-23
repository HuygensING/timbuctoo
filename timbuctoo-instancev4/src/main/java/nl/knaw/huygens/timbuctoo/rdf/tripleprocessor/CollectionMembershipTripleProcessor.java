package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
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

    Collection collection;
    Collection prevCollection;
    if (isAssertion) {
      prevCollection = database.getDefaultCollection(vreName);
      collection = database.findOrCreateCollection(vreName, triple.getObject());
    } else {
      prevCollection = database.findOrCreateCollection(vreName, triple.getObject());
      collection = database.getDefaultCollection(vreName);
    }

    entity.moveToCollection(prevCollection, collection);
  }
}
