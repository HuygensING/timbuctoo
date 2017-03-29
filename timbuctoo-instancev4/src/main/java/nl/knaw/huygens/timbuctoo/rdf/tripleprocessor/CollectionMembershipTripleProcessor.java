package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Collection;
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
      Collection newCollection = database.findOrCreateCollection(vreName, object, getLocalName(object));
      Collection defaultCollection = database.getDefaultCollection(vreName);
      entity.moveToNewCollection(defaultCollection, newCollection);
    }
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    Entity entity = database.findOrCreateEntity(vreName, subject);
    entity.removeFromCollection(database.findOrCreateCollection(vreName, object, getLocalName(object)));
  }
}
