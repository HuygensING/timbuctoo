package nl.knaw.huygens.timbuctoo.rdf;

public interface TripleImporter {
  void importTriple(boolean isAssertion, org.apache.jena.graph.Triple triple);
}
