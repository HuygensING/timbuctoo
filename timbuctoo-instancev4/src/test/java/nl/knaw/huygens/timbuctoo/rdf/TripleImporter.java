package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;

public class TripleImporter {

  private final TripleProcessorImpl processor;
  private final String vreName;

  public TripleImporter(GraphWrapper graphWrapper, String vreName) {
    this.processor = new TripleProcessorImpl(new Database(graphWrapper));
    this.vreName = vreName;
  }

  public void importTriple(Triple triple) {
    processor.process(vreName, triple);
  }
}
