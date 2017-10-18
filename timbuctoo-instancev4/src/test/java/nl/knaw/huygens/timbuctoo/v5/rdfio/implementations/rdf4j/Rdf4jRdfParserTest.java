package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j.parsers.NquadsUdParser.NquadsUdParserFactory;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Rdf4jRdfParserTest {

  private static final boolean DELETE = false;

  @Test
  public void supportsNQuadsUdDeletions() throws Exception {
    RDFParserRegistry.getInstance().add(new NquadsUdParserFactory());
    RdfProcessor rdfProcessor = mock(RdfProcessor.class);
    StringReader reader =
      new StringReader("-<http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .");
    Rdf4jRdfParser instance = new Rdf4jRdfParser();

    instance.importRdf(rdfPatchLog(reader), "", "", rdfProcessor);

    verify(rdfProcessor).onQuad(
      DELETE,
      "http://example.org/subject1",
      "http://pred",
      "12",
      "http://number",
      null,
      "http://some_graph"
    );
  }

  private CachedLog rdfPatchLog(StringReader reader) {
    return new CachedLog() {
      @Override
      public void close() throws Exception {

      }

      @Override
      public String getName() {
        return "http://example.com";
      }

      @Override
      public File getFile() {
        throw new UnsupportedOperationException("Method should not be needed");
      }

      @Override
      public Reader getReader() throws IOException {
        return reader;
      }

      @Override
      public MediaType getMimeType() {
        return new MediaType("application", "vnd.timbuctoo-rdf.nquads_unified_diff");
      }
    };
  }
}
