package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

class AddRelationTripleProcessor implements TripleProcessor {
  private final Database database;

  public AddRelationTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(Triple triple, String vreName) {
    Node node = triple.getSubject();
    Entity subject = database.findOrCreateEntity(vreName, node);
    Entity object = database.findOrCreateEntity(vreName, triple.getObject());

    final Relation relation = subject.addRelation(triple.getPredicate(), object);
    relation.setCommonVreProperties(vreName);
  }
}
