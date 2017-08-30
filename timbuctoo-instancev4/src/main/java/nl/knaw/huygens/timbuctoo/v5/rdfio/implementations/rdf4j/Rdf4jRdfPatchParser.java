package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is not a complete implementation of the RDF Patch draft: https://afs.github.io/rdf-patch/
 * We only support A(dd) & D(elete) in front of Triples or Quads.
 */
public class Rdf4jRdfPatchParser implements RdfParser {

  @Override
  public void importRdf(String cursorPrefix, String startFrom, CachedLog input, RdfProcessor rdfProcessor)
    throws RdfProcessingFailedException {
    List<String> actions = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(input.getReader());
      List<String> convertedStrings = reader.lines().map(line -> {
        if (line.startsWith("A ") || line.startsWith("D ")) {
          actions.add(line.substring(0, 1));
          return line.substring(2, line.length());
        }
        return line;
      }).collect(Collectors.toList());

      StringReader stringReader = new StringReader(String.join("\n", convertedStrings));
      RDFParser parser = Rio.createParser(RDFFormat.NQUADS);
      parser.setRDFHandler(new QuadHandlerDelegator(rdfProcessor, input.getName(), cursorPrefix, startFrom, actions));
      parser.parse(stringReader, input.getName().toString());

    } catch (IOException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  class QuadHandlerDelegator extends AbstractRDFHandler {

    private final RdfProcessor rdfProcessor;
    private final String fileUri;
    private final String cursorPrefix;
    private final int startFrom;
    private final List<String> actions;
    private int idx;

    private QuadHandlerDelegator(RdfProcessor rdfProcessor, String fileUri, String cursorPrefix, String startFrom,
                                 List<String> actions) {
      this.rdfProcessor = rdfProcessor;
      this.fileUri = fileUri;
      this.cursorPrefix = cursorPrefix;
      this.startFrom = startFrom.isEmpty() ? 0 : Integer.parseInt(startFrom);
      this.actions = actions;
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
          String graph = st.getContext() == null ? fileUri : st.getContext().stringValue();
          rdfProcessor.onQuad(
            "A".equals(actions.get(idx)),
            cursorPrefix + idx,
            st.getSubject().stringValue(),
            st.getPredicate().stringValue(),
            st.getObject().stringValue(),
            (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getDatatype().toString() : null,
            (st.getObject() instanceof Literal) ? ((Literal) st.getObject()).getLanguage().orElse(null) : null,
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
