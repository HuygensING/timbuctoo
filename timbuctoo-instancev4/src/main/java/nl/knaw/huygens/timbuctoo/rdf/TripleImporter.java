package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.core.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessor;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.jena.graph.Triple;

public class TripleImporter {

  private final TripleProcessor processor;
  private final String vreName;
  private final TimbuctooActions timbuctooActions;
  private boolean prepareWasCalled;


  public TripleImporter(TinkerPopGraphManager graphWrapper, String vreName, TimbuctooActions timbuctooActions) {
    this.processor = new TripleProcessorImpl(new Database(graphWrapper));
    this.vreName = vreName;
    this.timbuctooActions = timbuctooActions;
    this.prepareWasCalled = false;
  }

  public void prepare() {
    timbuctooActions.ensureVreExists(vreName);
    prepareWasCalled = true;
  }

  public void importTriple(boolean isAssertion, Triple triple) {
    if (!prepareWasCalled) {
      throw new RuntimeException("Call 'prepare' be for 'importTriple'");
    }
    processor.process(vreName, isAssertion, triple);
  }
}
