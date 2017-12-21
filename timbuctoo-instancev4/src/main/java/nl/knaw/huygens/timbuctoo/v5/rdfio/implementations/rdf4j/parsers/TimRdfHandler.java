package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j.parsers;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.util.function.Supplier;

public class TimRdfHandler extends AbstractRDFHandler {
  private static final int ADD = '+';
  private final RdfProcessor rdfProcessor;
  private final String defaultGraph;
  private Supplier<Integer> actionSupplier;
  private String fileName;

  public TimRdfHandler(RdfProcessor rdfProcessor, String defaultGraph, String fileName) {
    this.rdfProcessor = rdfProcessor;
    this.defaultGraph = defaultGraph;
    this.fileName = fileName;
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
  public void startRDF() throws RDFHandlerException {
  }

  @Override
  public void endRDF() throws RDFHandlerException {
  }

  @Override
  public void handleStatement(Statement st) throws RDFHandlerException {
    try {
      if (Thread.currentThread().isInterrupted()) {
        rdfProcessor.commit();
        throw new RDFHandlerException("Interrupted");
      }
      String graph = st.getContext() == null ? defaultGraph : st.getContext().stringValue();
      rdfProcessor.onQuad(
        isAssertion(),
        handleSubjectNode(st.getSubject()),
        st.getPredicate().stringValue(),
        handleObjectNode(st.getObject()),
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

  private String handleSubjectNode(Resource resource) {
    if (resource instanceof BNode) {
      String nodeName = resource.toString();
      String nodeId = nodeName.substring(nodeName.indexOf(":") + 1, nodeName.length());
      return "BlankNode:" + fileName + "/" + nodeId;
    } else {
      return resource.stringValue();
    }
  }

  private String handleObjectNode(Value value) {
    if (value instanceof BNode) {
      String nodeName = value.toString();
      String nodeId = nodeName.substring(nodeName.indexOf(":") + 1, nodeName.length());
      return "BlankNode:" + fileName + "/" + nodeId;
    } else {
      return value.stringValue();
    }
  }
}
