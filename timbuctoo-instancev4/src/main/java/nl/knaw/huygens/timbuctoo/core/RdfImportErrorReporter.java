package nl.knaw.huygens.timbuctoo.core;

public interface RdfImportErrorReporter {
  void entityTypeUnknown(String rdfUri);

  void entityHasWrongTypeForProperty(String entityRdfUri, String predicateUri, String expectedTypeUri,
                                     String actualTypeUri);

  void multipleRdfTypes(String subject, String object);
}
