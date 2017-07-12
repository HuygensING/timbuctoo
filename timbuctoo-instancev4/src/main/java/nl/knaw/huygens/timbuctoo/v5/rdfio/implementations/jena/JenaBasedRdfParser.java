package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.jena;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class JenaBasedRdfParser implements RdfParser {
  @Override
  public void importRdf(String cursorPrefix, String startFrom, CachedLog input, RdfProcessor rdfProcessor)
      throws RdfProcessingFailedException {
    try {
      Lang lang = input.getMimeType()
        .map((contentType) -> RDFLanguages.contentTypeToLang(contentType.toString()))
        .orElseThrow(
          () -> new UnsupportedRDFormatException(input.getName() + " does not look like a known rdf type.")
        );
      RDFDataMgr.parse(
        new QuadHandlerDelegator(rdfProcessor, input.getName().toString(), startFrom, cursorPrefix),
        input.getReader(),
        lang
      );
    } catch (Exception e) {
      if (e instanceof RdfProcessingFailedException) {
        throw (RdfProcessingFailedException) e;
      } else if (e.getCause() instanceof RdfProcessingFailedException) {
        throw (RdfProcessingFailedException) e.getCause();
      } else {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  private static class QuadHandlerDelegator implements StreamRDF {
    private final RdfProcessor rdfProcessor;
    private final String fileUri;
    private final int startFrom;
    private final String cursor;
    private int statementCounter;

    public QuadHandlerDelegator(RdfProcessor rdfProcessor, String fileUri, String startFrom, String cursorPrefix) {
      this.rdfProcessor = rdfProcessor;
      this.fileUri = fileUri;
      this.startFrom = startFrom.isEmpty() ? 0 : Integer.parseInt(startFrom);
      this.cursor = cursorPrefix;
      statementCounter = 0;
    }

    @Override
    public void start() {
      try {
        rdfProcessor.start();
      } catch (RdfProcessingFailedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void triple(Triple triple) {
      String subject = triple.getSubject().toString();
      String predicate = triple.getPredicate().toString();
      String object = triple.getObject().toString(false);
      String literalDataTypeUri = triple.getObject().isLiteral() ? triple.getObject().getLiteralDatatypeURI() : null;
      String language = triple.getObject().isLiteral() ? triple.getObject().getLiteralLanguage() : null;
      //Use the uri of the current file as the graph name if no graph name is specified
      sendQuad(subject, predicate, object, literalDataTypeUri, language, this.fileUri);
    }

    @Override
    public void quad(Quad quad) {
      String subject = quad.getSubject().toString();
      String predicate = quad.getPredicate().toString();
      String object = quad.getObject().toString(false);
      String literalDataTypeUri = quad.getObject().isLiteral() ? quad.getObject().getLiteralDatatypeURI() : null;
      String graph = quad.getGraph().toString();
      String language = quad.getObject().isLiteral() ? quad.getObject().getLiteralLanguage() : null;
      sendQuad(subject, predicate, object, literalDataTypeUri, language, graph);
    }

    private void sendQuad(String subject, String predicate, String object, String literalDataTypeUri, String language,
                          String graph) {
      try {
        if (Thread.currentThread().isInterrupted()) {
          rdfProcessor.finish();
          throw new RDFHandlerException("Interrupted");
        }
        if (statementCounter >= startFrom) {
          rdfProcessor.onQuad(
            true,
            cursor + statementCounter,
            subject,
            predicate,
            object,
            literalDataTypeUri,
            language,
            graph
          );
        }
        statementCounter++;
      } catch (RdfProcessingFailedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void base(String base) {
    }

    @Override
    public void prefix(String prefix, String iri) {
      try {
        if (statementCounter >= startFrom) {
          rdfProcessor.setPrefix(cursor + statementCounter, prefix, iri);
        }
        statementCounter++;
      } catch (RdfProcessingFailedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void finish() {
      try {
        rdfProcessor.finish();
      } catch (RdfProcessingFailedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
