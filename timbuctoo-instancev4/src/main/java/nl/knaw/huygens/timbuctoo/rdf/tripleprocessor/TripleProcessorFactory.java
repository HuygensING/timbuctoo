package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Triple;

public class TripleProcessorFactory {
  private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String RDF_SUB_CLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
  private Database database;

  public TripleProcessorFactory(Database database) {
    this.database = database;
  }

  public TripleProcessor getTripleProcessor(Triple triple) {
    // make sure the most concrete type stay on top
    if (describesType(triple)) {
      return new AddToCollectionTripleProcessor(database);
    } else if (describesRelationWithKnownArchetype(triple)) {
      return new AddToArchetypeTripleProcessor(database);
    } else if (describesProperty(triple)) {
      return new AddPropertyTripleProcessor(database);
    } else if (describesRelation(triple)) {
      return new AddRelationTripleProcessor(database);
    } else {
      return new UnsupportedTripleProcessor();
    }
  }

  private boolean describesRelationWithKnownArchetype(Triple triple) {
    return triple.getPredicate().getURI().equals(RDF_SUB_CLASS_OF) &&
      database.isKnownArchetype(triple.getObject().getLocalName());
  }

  private boolean describesRelation(Triple triple) {
    return triple.getObject().isURI();
  }

  private boolean describesProperty(Triple triple) {
    return triple.getObject().isLiteral();
  }

  private boolean describesType(Triple triple) {
    return triple.getPredicate().getURI().equals(RDF_TYPE);
  }

}
