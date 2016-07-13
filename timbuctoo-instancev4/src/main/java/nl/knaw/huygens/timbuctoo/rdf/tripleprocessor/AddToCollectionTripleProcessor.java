package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;

class AddToCollectionTripleProcessor implements TripleProcessor {
  private final Database database;

  public AddToCollectionTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(Triple triple, String vreName) {
    Entity entity = database.findOrCreateEntity(vreName, triple.getSubject());
    Collection collection = database.findOrCreateCollection(vreName, triple.getObject());
    entity.addToCollection(collection);
  }
}
