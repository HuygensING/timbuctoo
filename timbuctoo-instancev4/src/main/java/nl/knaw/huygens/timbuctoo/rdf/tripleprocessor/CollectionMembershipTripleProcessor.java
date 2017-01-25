package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.rdf.model.impl.Util;

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
      entity.addToCollection(database.findOrCreateCollection(vreName, object, getEntityTypeName(object)));
      entity.removeFromCollection(database.getDefaultCollection(vreName));
    }

  }

  private String getEntityTypeName(String object) {
    // We use the local name from the object of a type triple as the entity type name of a timbuctoo collection.
    return object.substring(Util.splitNamespaceXML(object));
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    Entity entity = database.findOrCreateEntity(vreName, subject);
    entity.removeFromCollection(database.findOrCreateCollection(vreName, object, getEntityTypeName(object)));
  }
}
