package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

class RelationTripleProcessor implements TripleProcessor{
  private static final Logger LOG = getLogger(RelationTripleProcessor.class);
  private final Database database;

  public RelationTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(String vreName, boolean isAssertion, Triple triple) {
    final Entity subject = database.findOrCreateEntity(vreName, triple.getSubject());
    Node predicate = triple.getPredicate();
    final RelationType relationType = database.findOrCreateRelationType(predicate);
    final Entity object;

    object = database.findOrCreateEntity(vreName, triple.getObject());

    if (isAssertion) {
      if (relationType.isInverted()) {
        final Relation relation = object.addRelation(relationType, subject);
        relation.setCommonVreProperties(vreName);
      } else {
        final Relation relation = subject.addRelation(relationType, object);
        relation.setCommonVreProperties(vreName);
      }
    } else {
      subject.removeRelation(relationType, object);
    }
  }
}
