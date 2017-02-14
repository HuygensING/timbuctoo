package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.RdfNameHelper.getLocalName;

class CollectionMembershipTripleProcessor extends AbstractReferenceTripleProcessor {
  private final Database database;
  private final RdfImportSession rdfImportSession;

  public CollectionMembershipTripleProcessor(Database database, RdfImportSession rdfImportSession) {
    this.database = database;
    this.rdfImportSession = rdfImportSession;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String object) {
    Entity entity = database.findOrCreateEntity(vreName, subject);

    if (entity.isInKnownCollection()) {
      rdfImportSession.getErrorReporter().multipleRdfTypes(subject, object);
    } else {
      entity.addToCollection(database.findOrCreateCollection(vreName, object, getLocalName(object)));
      entity.removeFromCollection(database.getDefaultCollection(vreName));
    }
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    Entity entity = database.findOrCreateEntity(vreName, subject);
    entity.removeFromCollection(database.findOrCreateCollection(vreName, object, getLocalName(object)));
  }
}
