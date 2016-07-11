package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessor;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;

public class Importer {

  private final TripleProcessorFactory tripleProcessorFactory;

  public Importer(GraphWrapper graphWrapper) {
    this.tripleProcessorFactory = new TripleProcessorFactory(graphWrapper);
  }

  public void importTriple(Triple triple) {
    TripleProcessor processor = tripleProcessorFactory.getTripleProcessor(triple);
    processor.process(triple);
  }


}
