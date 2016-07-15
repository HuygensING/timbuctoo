package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TripleHelper {
  public static Triple createSingleTriple(String tripleString) {
    return createTripleIterator(tripleString).next();
  }

  public static ExtendedIterator<Triple> createTripleIterator(String tripleString) {
    Model model = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model.read(in, null, "N3");
    return model.getGraph().find(Triple.ANY);
  }

}
