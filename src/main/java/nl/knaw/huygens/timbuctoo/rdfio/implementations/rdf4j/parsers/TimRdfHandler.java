package nl.knaw.huygens.timbuctoo.rdfio.implementations.rdf4j.parsers;

import nl.knaw.huc.rdf4j.rio.nquadsnd.RDFAssertionHandler;
import nl.knaw.huygens.timbuctoo.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import static nl.knaw.huygens.timbuctoo.util.BNodeHelper.createSkolomIri;

public class TimRdfHandler extends AbstractRDFHandler implements RDFAssertionHandler {
  private final RdfProcessor rdfProcessor;
  private final String baseUri;
  private final String defaultGraph;
  private final String fileName;

  public TimRdfHandler(RdfProcessor rdfProcessor, String baseUri, String defaultGraph, String fileName) {
    this.rdfProcessor = rdfProcessor;
    this.baseUri = baseUri;
    this.defaultGraph = defaultGraph;
    this.fileName = fileName;
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
  public void handleStatement(Statement st) throws RDFHandlerException {
    handleStatement(true, st);
  }

  @Override
  public void handleStatement(boolean isAssertion, Statement st) throws RDFHandlerException {
    try {
      if (Thread.currentThread().isInterrupted()) {
        rdfProcessor.commit();
        throw new RDFHandlerException("Interrupted");
      }

      String graph = st.getContext() == null ? defaultGraph : st.getContext().stringValue();

      // Make sure old default graphs (based on the base URI) also end up in the configured default graph
      String baseUriGraphV1 = baseUri.endsWith("/") ? baseUri : baseUri + "/";
      String baseUriGraphV2 = !baseUri.endsWith("/") ? baseUri : baseUri.substring(0, baseUri.length() - 1);
      if (graph != null && (graph.equals(baseUriGraphV1) || graph.equals(baseUriGraphV2))) {
        graph = defaultGraph;
      }

      rdfProcessor.onQuad(
          isAssertion,
          handleNode(st.getSubject()),
          st.getPredicate().stringValue(),
          handleNode(st.getObject()),
          (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getDatatype().toString() : null,
          (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getLanguage().orElse(null) : null,
          graph
      );
    } catch (RdfProcessingFailedException e) {
      throw new RDFHandlerException(e);
    }
  }

  private String handleNode(Value resource) {
    return resource instanceof BNode ? createSkolomIri(baseUri, fileName, (BNode) resource) : resource.stringValue();
  }
}
