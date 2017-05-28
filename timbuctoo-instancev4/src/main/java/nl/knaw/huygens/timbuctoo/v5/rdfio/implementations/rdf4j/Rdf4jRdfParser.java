package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.eclipse.rdf4j.rio.Rio.getParserFormatForFileName;

public class Rdf4jRdfParser implements RdfParser {
  @Override
  public void importRdf(String cursorPrefix, String startFrom, CachedLog input, RdfProcessor rdfProcessor)
    throws RdfProcessingFailedException {

    Optional<RDFFormat> format = input.getMimeType()
      .flatMap(mimeType -> Rio.getParserFormatForMIMEType(mimeType.toString()));
    URI name = input.getName();
    if (!format.isPresent()) {
      format = getParserFormatForFileName(name.toString());
    }
    try {
      RDFFormat unwrappedFormat = format.orElseThrow(
        () -> new UnsupportedRDFormatException(name.toString() + " does not look like a known rdf type.")
      );
      RDFParser rdfParser = Rio.createParser(unwrappedFormat);
      rdfParser.setPreserveBNodeIDs(true);
      rdfParser.setRDFHandler(new QuadHandlerDelegator(rdfProcessor, name, cursorPrefix, startFrom));
      rdfParser.parse(input.getReader(), name.toString());
    } catch (IOException | RDFParseException | UnsupportedRDFormatException e) {
      throw new RdfProcessingFailedException(e);
    } catch (RDFHandlerException e) {
      if (e.getCause() instanceof RdfProcessingFailedException) {
        throw (RdfProcessingFailedException) e.getCause();
      } else {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  class QuadHandlerDelegator extends AbstractRDFHandler {

    private final RdfProcessor rdfProcessor;
    private final URI fileUri;
    private final String cursorPrefix;
    private final int startFrom;
    private long idx;

    private QuadHandlerDelegator(RdfProcessor rdfProcessor, URI fileUri, String cursorPrefix, String startFrom) {
      this.rdfProcessor = rdfProcessor;
      this.fileUri = fileUri;
      this.cursorPrefix = cursorPrefix;
      this.startFrom = Integer.parseInt(startFrom);
      this.idx = 0;
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
          String graph = st.getContext() == null ? fileUri.toString() : st.getContext().stringValue();
          rdfProcessor.onQuad(
            true,
            cursorPrefix + idx,
            st.getSubject().stringValue(),
            st.getPredicate().stringValue(),
            st.getObject().stringValue(),
            (st.getObject() instanceof Literal) ? ((Literal) st).getDatatype().toString() : null,
            (st.getObject() instanceof Literal) ? ((Literal) st).getLanguage().orElse(null) : null,
            graph
          );
        }
        idx++;
      } catch (RdfProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

  }
}
