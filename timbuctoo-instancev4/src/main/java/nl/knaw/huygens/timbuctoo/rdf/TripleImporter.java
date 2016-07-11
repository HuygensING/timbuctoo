package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessor;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;

class TripleImporter {

  private final TripleProcessorFactory tripleProcessorFactory;
  private final GraphWrapper graphWrapper;
  private final String vreName;

  public TripleImporter(GraphWrapper graphWrapper, String vreName) {
    this.tripleProcessorFactory = new TripleProcessorFactory(graphWrapper);
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
  }

  public void importTriple(Triple triple) {
    TripleProcessor processor = tripleProcessorFactory.getTripleProcessor(triple);
    processor.process(triple);
  }
}
