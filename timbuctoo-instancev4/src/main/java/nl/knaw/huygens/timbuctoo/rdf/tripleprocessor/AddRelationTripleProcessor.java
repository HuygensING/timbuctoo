package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.SystemPropertyModifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.time.Clock;

class AddRelationTripleProcessor implements TripleProcessor {
  private final Database database;
  private final SystemPropertyModifier systemPropertyModifier;

  public AddRelationTripleProcessor(Database database) {
    this.database = database;
    systemPropertyModifier = new SystemPropertyModifier(Clock.systemDefaultZone());
  }

  @Override
  public void process(Triple triple, String vreName) {
    Node node = triple.getSubject();
    Entity subject = database.findOrCreateEntity(vreName, node);
    Entity object = database.findOrCreateEntity(vreName, triple.getObject());

    subject.addRelation(triple.getPredicate(), object);

  }
}
