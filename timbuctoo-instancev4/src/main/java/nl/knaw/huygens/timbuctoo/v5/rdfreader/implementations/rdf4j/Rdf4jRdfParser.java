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
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import static org.eclipse.rdf4j.rio.Rio.getParserFormatForFileName;

public class Rdf4jRdfParser implements RdfParser {
  @Override
  public void importRdf(URI fileUri, Optional<String> mimeType, InputStream data, QuadHandler quadHandler)
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
      rdfParser.setRDFHandler(new QuadHandlerDelegator(quadHandler));
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

    private QuadHandlerDelegator(QuadHandler quadHandler) {
      this.quadHandler = quadHandler;
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
      try {
        quadHandler.onPrefix(prefix, uri);
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

    @Override
    public void startRDF() throws RDFHandlerException {
      try {
        quadHandler.start();
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
      String valueType = null;
      if (st.getObject() instanceof Literal) {
        valueType = ((Literal) st.getObject()).getDatatype().toString();
      }
      try {
        quadHandler.onQuad(
          st.getSubject().stringValue(),
          st.getPredicate().stringValue(),
          st.getObject().stringValue(),
          valueType,
          st.getContext().stringValue()
        );
      } catch (LogProcessingFailedException e) {
        throw new RDFHandlerException(e);
      }
    }

  }
}
