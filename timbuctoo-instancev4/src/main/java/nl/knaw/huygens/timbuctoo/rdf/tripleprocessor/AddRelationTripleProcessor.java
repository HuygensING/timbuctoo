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

    nl.knaw.huygens.timbuctoo.rdf.Relation relation = subject.addRelation(triple.getPredicate(), object);

    // Node node = triple.getSubject();
    // final Vertex subjectVertex = database.findOrCreateEntityVertex(node, CollectionDescription.getDefault(vreName));
    // final Vertex objectVertex =
    //   database.findOrCreateEntityVertex(triple.getObject(), CollectionDescription.getDefault(vreName));
    //
    // final Edge relationEdge = subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
    // relationEdge.property(Database.RDF_URI_PROP, triple.getPredicate().getURI());
    // systemPropertyModifier.setCreated(relationEdge, "rdf-importer");
    // systemPropertyModifier.setModified(relationEdge, "rdf-importer");
    // systemPropertyModifier.setRev(relationEdge, 1);
    // systemPropertyModifier.setIsDeleted(relationEdge, false);
    // systemPropertyModifier.setIsLatest(relationEdge, true);
    // systemPropertyModifier.setTimId(relationEdge);
  }
}
