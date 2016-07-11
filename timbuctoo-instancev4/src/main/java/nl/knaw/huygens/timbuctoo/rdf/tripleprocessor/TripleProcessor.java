package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.Triple;

public interface TripleProcessor {
  void process(Triple triple);
}
