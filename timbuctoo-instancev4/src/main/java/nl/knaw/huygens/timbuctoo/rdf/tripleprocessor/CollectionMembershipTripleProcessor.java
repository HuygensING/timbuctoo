package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;

class CollectionMembershipTripleProcessor {
  private final Database database;
  private final RdfImportSession rdfImportSession;

  public CollectionMembershipTripleProcessor(Database database, RdfImportSession rdfImportSession) {
    this.database = database;
    this.rdfImportSession = rdfImportSession;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Entity entity = database.findOrCreateEntity(vreName, triple.getSubject());

    if (isAssertion) {
      if (entity.isInKnownCollection()) {
        rdfImportSession.getErrorReporter().multipleRdfTypes(triple);
      } else {
        entity.addToCollection(database.findOrCreateCollection(vreName, triple.getObject()));
        entity.removeFromCollection(database.getDefaultCollection(vreName));
      }
    } else {
      entity.removeFromCollection(database.findOrCreateCollection(vreName, triple.getObject()));
    }
  }
}
