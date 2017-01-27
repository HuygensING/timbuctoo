package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.RdfNameHelper.getLocalName;

class RelationTripleProcessor extends AbstractReferenceTripleProcessor {
  private final Database database;

  public RelationTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String object) {
    final Entity subjectEntity = database.findOrCreateEntity(vreName, subject);
    final RelationType relationType = database.findOrCreateRelationType(predicate, getLocalName(predicate));
    final Entity objectEntity = database.findOrCreateEntity(vreName, object);

    if (relationType.isInverted()) {
      final Relation relation = objectEntity.addRelation(relationType, subjectEntity);
      relation.setCommonVreProperties(vreName);
    } else {
      final Relation relation = subjectEntity.addRelation(relationType, objectEntity);
      relation.setCommonVreProperties(vreName);
    }
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    final Entity subjectEntity = database.findOrCreateEntity(vreName, subject);
    final RelationType relationType = database.findOrCreateRelationType(predicate, getLocalName(predicate));
    final Entity objectEntity = database.findOrCreateEntity(vreName, object);

    subjectEntity.removeRelation(relationType, objectEntity);
  }

}
