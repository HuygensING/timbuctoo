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
    String name = input.getName();
    if (!format.isPresent()) {
      format = getParserFormatForFileName(name.toString());
    }
    try {
      RDFFormat unwrappedFormat = format.orElseThrow(
        () -> new UnsupportedRDFormatException(name.toString() + " does not look like a known rdf type.")
      );
      RDFParser rdfParser = Rio.createParser(unwrappedFormat);
      rdfParser.setPreserveBNodeIDs(true);
      int startFromInt = startFrom.isEmpty() ? 0 : Integer.parseInt(startFrom);
      rdfParser.setRDFHandler(new TimRdfHandler(rdfProcessor, name, cursorPrefix, startFromInt));
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
}
