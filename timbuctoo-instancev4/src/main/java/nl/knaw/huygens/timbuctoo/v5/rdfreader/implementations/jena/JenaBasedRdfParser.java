package nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.jena;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import java.io.Reader;
import java.net.URI;
import java.util.Optional;

public class JenaBasedRdfParser implements RdfParser {
  @Override
  public void loadFile(URI fileUri, Optional<String> mimeType, Reader data, QuadHandler quadHandler) {
    Lang lang = mimeType
      .map(RDFLanguages::contentTypeToLang)
      .orElseThrow(
        () -> new UnsupportedRDFormatException(fileUri.toString() + " does not look like a known rdf type.")
      );
    RDFDataMgr.parse(
      new RdfStreamReader(quadHandler, fileUri.toString()),
      new ReaderInputStream(data, Charsets.UTF_8),
      lang
    );
  }
}
