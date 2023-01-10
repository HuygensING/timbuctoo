package nl.knaw.huygens.timbuctoo.implementations.rdf4j.parsers;

import nl.knaw.huc.rdf4j.rio.nquadsnd.NQuadsUdParser;
import nl.knaw.huygens.timbuctoo.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.rdfio.implementations.rdf4j.parsers.TimRdfHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NquadsUdParserTest {
  private static final String BASE_URI = "http://example.org";
  private RdfProcessor rdfProcessor;
  private NQuadsUdParser instance;

  @BeforeEach
  public void setUp() throws Exception {
    rdfProcessor = mock(RdfProcessor.class);
    instance = new NQuadsUdParser();
    instance.setRDFHandler(new TimRdfHandler(rdfProcessor, "http://example.org/file",
        "http://example.org/file/", "", false));
  }

  @Test
  public void parseStripsTheActionAddsItToTheActionsHolder() throws Exception {
    instance.setRDFHandler(new TimRdfHandler(rdfProcessor, "http://example.org/file",
        "http://example.org/file/","", false));
    StringReader reader =
      new StringReader("-<http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .");

    instance.parse(reader, "http://example.org/");

    verify(rdfProcessor).onQuad(
      false,
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
    StringReader reader =
      new StringReader("+<http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .");

    instance.parse(reader, "http://example.org/");

    verify(rdfProcessor).onQuad(
      true,
      "http://example.org/subject1",
      "http://pred",
      "12",
      "http://number",
      null,
      "http://some_graph"
    );
  }

  @Test
  public void itIgnoresLinesThatStartWithoutAPlusOrAMinus() throws Exception {
    StringReader reader = new StringReader(
      " <http://example.org/subject1> <http://pred> \"12\"^^<http://number> <http://some_graph> .\n" +
        "@@ -1,4 +1,4 @@"
    );

    instance.parse(reader, "http://example.org/");

    verify(rdfProcessor, never()).onQuad(
      anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
    );
  }

  @Test
  public void itIgnoresLinesThatStartWithTriplePlusSigns() throws Exception {
    StringReader reader = new StringReader("+++ fruits2\t2017-08-16 11:38:05.327645535 +0200");

    instance.parse(reader, "http://example.org/");

    verify(rdfProcessor, never()).onQuad(
      anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
    );
  }

  @Test
  public void itIgnoresLinesThatStartWithTripleMinusSigns() throws Exception {
    StringReader reader = new StringReader("--- fruits1\t2017-08-16 11:37:47.247741827 +0200");

    instance.parse(reader, "http://example.org/");

    verify(rdfProcessor, never()).onQuad(
      anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
    );
  }

  @Test
  public void itIgnoresLinesThatStartWithABackslash() throws Exception {
    StringReader reader = new StringReader("\\ No newline at end of file");

    instance.parse(reader, BASE_URI);

    verify(rdfProcessor, never()).onQuad(
      anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
    );
  }
}
