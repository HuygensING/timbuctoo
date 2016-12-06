package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessor;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.jena.graph.Triple;

import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;

public class TripleImporter {

  private final TransactionEnforcer transactionEnforcer;
  private final TripleProcessor processor;
  private final TinkerPopGraphManager graphWrapper;
  private final String vreName;
  private boolean prepareWasCalled;


  public TripleImporter(TransactionEnforcer transactionEnforcer, TinkerPopGraphManager graphWrapper, String vreName) {
    this.transactionEnforcer = transactionEnforcer;
    this.processor = new TripleProcessorImpl(new Database(graphWrapper));
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
    this.prepareWasCalled = false;
  }

  public void prepare() {
    transactionEnforcer.execute(timbuctooActions -> {
      timbuctooActions.ensureVreExists(vreName);
      return commit();
    });
    prepareWasCalled = true;
  }

  public void importTriple(boolean isAssertion, Triple triple) {
    if (!prepareWasCalled) {
      throw new RuntimeException("Call 'prepare' be for 'importTriple'");
    }
    processor.process(vreName, isAssertion, triple);
  }
}
