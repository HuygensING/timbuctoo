package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;

public class TripleProcessorFactory {
  private Database database;

  public TripleProcessorFactory(GraphWrapper graphWrapper) {
    this.database = new Database(graphWrapper);
  }

  public TripleProcessor getTripleProcessor(Triple triple) {
    if (describesType(triple)) {
      return new AddToCollectionTripleProcessor(database);
    } else if (describesProperty(triple)) {
      return new AddPropertyTripleProcessor(database);
    } else if (describesRelation(triple)) {
      return new AddRelationTripleProcessor(database);
    } else {
      return new UnsupportedTripleProcessor();
    }
  }

  boolean describesRelation(Triple triple) {
    return triple.getObject().isURI();
  }

  boolean describesProperty(Triple triple) {
    return triple.getObject().isLiteral();
  }

  boolean describesType(Triple triple) {
    return triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
  }

}
