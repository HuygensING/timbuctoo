package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Rdf4jRdfPatchParserTest {

  private static final String START_FROM = "";
  private static final String CURSOR_PREFIX = "";

  @Test
  public void itDeletes() throws Exception {
    RdfProcessor rdfProcessor = mock(RdfProcessor.class);
    StringReader reader =
      new StringReader("D <http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .");
    CachedLog cachedLog = rdfPatchLog(reader);
    Rdf4jRdfPatchParser instance = new Rdf4jRdfPatchParser();

    instance.importRdf(CURSOR_PREFIX, START_FROM, cachedLog, rdfProcessor);

    verify(rdfProcessor).onQuad(
      false,
      "0",
      "http://example.org/subject1",
      "http://pred",
      "12",
      "http://number",
      null,
      "http://some_graph"
    );
  }

  @Test
  public void itAdds() throws Exception {
    RdfProcessor rdfProcessor = mock(RdfProcessor.class);
    StringReader reader =
      new StringReader("A <http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .");
    CachedLog cachedLog = rdfPatchLog(reader);
    Rdf4jRdfPatchParser instance = new Rdf4jRdfPatchParser();

    instance.importRdf(CURSOR_PREFIX, START_FROM, cachedLog, rdfProcessor);

    verify(rdfProcessor).onQuad(
      true,
      "0",
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
      public URI getName() {
        return URI.create("http://example.com");
      }

      @Override
      public Reader getReader() throws IOException {
        return reader;
      }

      @Override
      public Optional<MediaType> getMimeType() {
        return Optional.of(new MediaType("application", "rdf-patch"));
      }
    };
  }
}
