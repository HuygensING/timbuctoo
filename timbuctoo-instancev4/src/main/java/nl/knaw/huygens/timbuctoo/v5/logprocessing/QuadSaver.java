package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;

public interface QuadSaver {
  void start() throws LogStorageFailedException;

  void onPrefix(String prefix, String iri) throws LogStorageFailedException;

  void onRelation(String subject, String predicate, String object, String graph)
    throws LogStorageFailedException;

  void onLiteral(String subject, String predicate, String value, String valueType, String graph)
    throws LogStorageFailedException;

  void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws LogStorageFailedException;

  default void onTriple(String subject, String predicate, String object, String valueType, String language,
                        String graph) throws LogStorageFailedException {
    if (valueType == null) {
      this.onRelation(subject, predicate, object, graph);
    } else if (RdfConstants.LANGSTRING.equals(valueType)) {
      this.onLanguageTaggedString(subject, predicate, object, language, graph);
    } else {
      this.onLiteral(subject, predicate, object, valueType, graph);
    }
  }

  void finish() throws LogStorageFailedException;

}
