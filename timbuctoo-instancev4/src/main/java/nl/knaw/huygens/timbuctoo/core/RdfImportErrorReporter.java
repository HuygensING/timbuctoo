package nl.knaw.huygens.timbuctoo.core;

import org.apache.jena.graph.Triple;

public interface RdfImportErrorReporter {
  void entityTypeUnknown(String rdfUri);

  void entityHasWrongTypeForProperty(String entityRdfUri, String predicateUri, String expectedTypeUri,
                                     String actualTypeUri);

  void multipleRdfTypes(Triple triple);
}
