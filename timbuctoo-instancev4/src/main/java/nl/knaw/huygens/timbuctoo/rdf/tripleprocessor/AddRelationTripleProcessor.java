package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;
import org.apache.jena.graph.Triple;

class AddRelationTripleProcessor {
  private final Database database;

  public AddRelationTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, Triple triple) {
    final Entity subject = database.findOrCreateEntity(vreName, triple.getSubject());
    final Entity object = database.findOrCreateEntity(vreName, triple.getObject());
    final RelationType relationType = database.findOrCreateRelationType(triple.getPredicate());

    final Relation relation = subject.addRelation(relationType, object);
    relation.setCommonVreProperties(vreName);
  }
}
