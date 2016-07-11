package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.Triple;

public interface TripleProcessor {
  public static final String RDF_URI_PROP = "rdfUri";
  void process(Triple triple);
}
