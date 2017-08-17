package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.util.function.Supplier;

public class TimRdfHandler extends AbstractRDFHandler {
  private static final int ADD = '+';
  private final RdfProcessor rdfProcessor;
  private final String fileUri;
  private final String cursorPrefix;
  private final int startFrom;
  private Supplier<Integer> actionSupplier;
  private int idx;

  public TimRdfHandler(RdfProcessor rdfProcessor, String fileUri, String cursorPrefix, int startFrom) {
    this.rdfProcessor = rdfProcessor;
    this.fileUri = fileUri;
    this.cursorPrefix = cursorPrefix;
    this.startFrom = startFrom;
    this.idx = 0;
  }

  public void registerActionSupplier(Supplier<Integer> actionSupplier) {
    this.actionSupplier = actionSupplier;
  }

  @Override
  public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    try {
      if (idx >= startFrom) {
        rdfProcessor.setPrefix(cursorPrefix + idx, prefix, uri);
      }
      idx++;
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  @Override
  public void startRDF() throws RDFHandlerException {
    try {
      rdfProcessor.start();
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  @Override
  public void endRDF() throws RDFHandlerException {
    try {
      rdfProcessor.finish();
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  @Override
  public void handleStatement(Statement st) throws RDFHandlerException {
    try {
      if (Thread.currentThread().isInterrupted()) {
        rdfProcessor.finish();
        throw new RDFHandlerException("Interrupted");
      }
      if (idx >= startFrom) {
        String graph = st.getContext() == null ? fileUri : st.getContext().stringValue();
        rdfProcessor.onQuad(
          isAssertion(),
          cursorPrefix + idx,
          st.getSubject().stringValue(),
          st.getPredicate().stringValue(),
          st.getObject().stringValue(),
          (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getDatatype().toString() : null,
          (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getLanguage().orElse(null) : null,
          graph
        );
      }
      idx++;
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  private boolean isAssertion() {
    return actionSupplier == null || actionSupplier.get() == ADD;
  }
}
