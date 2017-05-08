package nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;
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
import java.io.Reader;
import java.net.URI;
import java.util.Optional;

import static org.eclipse.rdf4j.rio.Rio.getParserFormatForFileName;

public class Rdf4jRdfParser implements RdfParser {
  @Override
  public void loadFile(URI fileUri, Optional<String> mimeType, Reader data, QuadHandler quadHandler)
      throws LogProcessingFailedException {

    Optional<RDFFormat> format = mimeType.flatMap(Rio::getParserFormatForMIMEType);
    if (!format.isPresent()) {
      format = getParserFormatForFileName(fileUri.toString());
    }
    try {
      RDFFormat unwrappedFormat = format.orElseThrow(
        () -> new UnsupportedRDFormatException(fileUri.toString() + " does not look like a known rdf type.")
      );
      RDFParser rdfParser = Rio.createParser(unwrappedFormat);
      rdfParser.setPreserveBNodeIDs(true);
      rdfParser.setRDFHandler(new QuadHandlerDelegator(quadHandler, fileUri));
      rdfParser.parse(data, fileUri.toString());
    } catch (IOException | RDFParseException | UnsupportedRDFormatException e) {
      throw new LogProcessingFailedException(e);
    } catch (RDFHandlerException e) {
      if (e.getCause() instanceof LogProcessingFailedException) {
        throw (LogProcessingFailedException) e.getCause();
      } else {
        throw new LogProcessingFailedException(e);
      }
    }
  }

  class QuadHandlerDelegator extends AbstractRDFHandler {

    private final QuadHandler quadHandler;
    private final URI fileUri;
    private long idx;

    private QuadHandlerDelegator(QuadHandler quadHandler, URI fileUri) {
      this.quadHandler = quadHandler;
      this.fileUri = fileUri;
      this.idx = 0;
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
      try {
        quadHandler.onPrefix(idx++, prefix, uri);
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

    @Override
    public void startRDF() throws RDFHandlerException {
      try {
        quadHandler.start(0);
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
      try {
        quadHandler.finish();
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
      try {
        if (Thread.currentThread().isInterrupted()) {
          quadHandler.cancel();
          throw new RDFHandlerException("Interrupted");
        }
        String graph = st.getContext() == null ? fileUri.toString() : st.getContext().stringValue();
        if (st.getObject() instanceof Literal) {
          Literal literal = (Literal) st.getObject();
          String valueType = literal.getDatatype().toString();
          if (literal.getLanguage().isPresent()) {
            quadHandler.onLanguageTaggedString(
              idx++,
              st.getSubject().stringValue(),
              st.getPredicate().stringValue(),
              st.getObject().stringValue(),
              literal.getLanguage().get(),
              graph
            );
          } else {
            quadHandler.onLiteral(
              idx++,
              st.getSubject().stringValue(),
              st.getPredicate().stringValue(),
              st.getObject().stringValue(),
              valueType,
              graph
            );
          }
        } else {
          quadHandler.onRelation(
            idx++,
            st.getSubject().stringValue(),
            st.getPredicate().stringValue(),
            st.getObject().stringValue(),
            graph
          );
        }
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

  }
}
