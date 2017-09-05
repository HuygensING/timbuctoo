package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j.parsers;

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
  private Supplier<Integer> actionSupplier;

  public TimRdfHandler(RdfProcessor rdfProcessor, String fileUri) {
    this.rdfProcessor = rdfProcessor;
    this.fileUri = fileUri;
  }

  public void registerActionSupplier(Supplier<Integer> actionSupplier) {
    this.actionSupplier = actionSupplier;
  }

  @Override
  public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    try {
      rdfProcessor.setPrefix(prefix, uri);
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  @Override
  public void startRDF() throws RDFHandlerException {}

  @Override
  public void endRDF() throws RDFHandlerException {}

  @Override
  public void handleStatement(Statement st) throws RDFHandlerException {
    try {
      if (Thread.currentThread().isInterrupted()) {
        rdfProcessor.commit();
        throw new RDFHandlerException("Interrupted");
      }
      String graph = st.getContext() == null ? fileUri : st.getContext().stringValue();
      rdfProcessor.onQuad(
        isAssertion(),
        st.getSubject().stringValue(),
        st.getPredicate().stringValue(),
        st.getObject().stringValue(),
        (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getDatatype().toString() : null,
        (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getLanguage().orElse(null) : null,
        graph
      );
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  private boolean isAssertion() {
    return actionSupplier == null || actionSupplier.get() == ADD;
  }
}
