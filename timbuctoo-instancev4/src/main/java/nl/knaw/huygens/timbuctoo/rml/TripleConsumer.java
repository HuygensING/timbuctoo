package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Triple;

public interface TripleConsumer {
  void accept(Triple triple);
}
