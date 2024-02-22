package nl.knaw.huygens.timbuctoo.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingParseException;
import nl.knaw.huygens.timbuctoo.rdfio.implementations.rdf4j.parsers.TimRdfHandler;
import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.rdfio.RdfParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import java.io.IOException;

public class Rdf4jRdfParser implements RdfParser {
  @Override
  public void importRdf(CachedLog input, String baseUri, String defaultGraph, RdfProcessor rdfProcessor)
    throws RdfProcessingFailedException, RdfProcessingParseException {

    try {
      RDFFormat format = Rio.getParserFormatForMIMEType(input.mimeType().toString())
        .orElseThrow(
          () -> new UnsupportedRDFormatException(input.mimeType() + " is not a supported rdf type.")
        );
      RDFParser rdfParser = Rio.createParser(format);
      rdfParser.setPreserveBNodeIDs(true);
      rdfParser.setRDFHandler(new TimRdfHandler(rdfProcessor, baseUri, defaultGraph, input.file().getName()));
      rdfParser.parse(input.getReader(), baseUri);
    } catch (IOException | UnsupportedRDFormatException e) {
      throw new RdfProcessingFailedException(e);
    } catch (RDFParseException e) {
      throw new Rdf4jRdfProcessingParseException(e, input);
    } catch (RDFHandlerException e) {
      if (e.getCause() instanceof RdfProcessingFailedException) {
        throw (RdfProcessingFailedException) e.getCause();
      } else {
        throw new RdfProcessingFailedException(e);
      }
    }
  }
}
