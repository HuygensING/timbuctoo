package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessor;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class TripleImporter {

  private static final Logger LOG = getLogger(TripleImporter.class);
  private final TripleProcessor processor;
  private final String vreName;


  public TripleImporter(TinkerPopGraphManager graphWrapper, String vreName, RdfImportSession rdfImportSession) {
    this.processor = new TripleProcessorImpl(new Database(graphWrapper), rdfImportSession);
    this.vreName = vreName;
  }

  public void importTriple(boolean isAssertion, Triple triple) {
    try {
      processor.process(vreName, isAssertion, triple);
    } catch (Throwable e) {
      LOG.error("Exception while processing triple", e);
      throw e;
    }
  }

}
