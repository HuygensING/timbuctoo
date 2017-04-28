package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TripleFileImporter implements TripleImporter {

  private final StreamRDF writerStream;

  public TripleFileImporter(String vreName) throws FileNotFoundException {
    writerStream = StreamRDFWriter.getWriterStream(new FileOutputStream(vreName + ".nt"), Lang.NTRIPLES);
    writerStream.start();
  }

  @Override
  public void importTriple(boolean isAssertion, Triple triple) {
    writerStream.triple(triple);
  }

  public void close() {
    writerStream.finish();
  }
}
