package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.Triple;

class UnsupportedTripleProcessor implements TripleProcessor {

  @Override
  public void process(Triple triple, String vreName) {
    // TODO find a better way to handle unsupported triples
    throw new IllegalArgumentException("Triple '" + triple + "' is unsupported.");
  }
}
