package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;

public class TripleCreator {

  public static Triple createSingleTripleWithLiteralObject(String subject, String predicate, String object) {
    return createSingleTriple("<" + subject + "> " + "<" + predicate + "> " + object + ".");
  }


  public static Triple createSingleTripleWithLiteralObject(
    String subject,
    String predicate,
    String value,
    String type
  ) {
    return new Triple(createURI(subject), createURI(predicate), createLiteral(value, new BaseDatatype(type)));
  }

  public static Triple createSingleTriple(String subject, String predicate, String object) {
    return createSingleTriple("<" + subject + "> " + "<" + predicate + "> " + "<" + object + "> .");
  }

  public static Triple createSingleTriple(String tripleString) {
    return createTripleIterator(tripleString).next();
  }

  public static ExtendedIterator<Triple> createTripleIterator(String tripleString) {
    Model model = ModelFactory.createDefaultModel();
    InputStream in = new ByteArrayInputStream(tripleString.getBytes(StandardCharsets.UTF_8));
    model.read(in, null, Lang.NQUADS.getName());
    return model.getGraph().find(Triple.ANY);
  }

  static Node createBlankNode() {
    Model model = ModelFactory.createDefaultModel();
    return model.createResource().asNode();
  }
}
