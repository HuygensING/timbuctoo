package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;

public class TripleProcessorFactory {
  private GraphWrapper graphWrapper;

  public TripleProcessorFactory(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public TripleProcessor getTripleProcessor(Triple triple) {
    if (describesType(triple)) {
      return new AddToCollectionTripleProcessor(new Database(graphWrapper));
    } else if (describesProperty(triple)) {
      return new AddPropertyTripleProcessor(new Database(graphWrapper));
    } else if (describesRelation(triple)) {
      return new AddRelationTripleProcessor(new Database(graphWrapper));
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
