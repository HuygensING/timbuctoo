package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

public interface RdfProcessor {

  void setPrefix(String cursor, String prefix, String iri) throws RdfProcessingFailedException;

  void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException;

  void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException;

  void addLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                               String graph) throws RdfProcessingFailedException;

  void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException;

  void delValue(String cursor, String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException;

  void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                               String graph) throws RdfProcessingFailedException;

  default void onQuad(boolean isAssertion, String cursor, String subject, String predicate, String object,
                      String dataType, String language, String graph) throws RdfProcessingFailedException {
    if (isAssertion) {
      if (dataType == null || dataType.isEmpty()) {
        this.addRelation(cursor, subject, predicate, object, graph);
      } else {
        if (language != null && !language.isEmpty() && dataType.equals(RdfConstants.LANGSTRING)) {
          this.addLanguageTaggedString(cursor, subject, predicate, object, language, graph);
        } else {
          this.addValue(cursor, subject, predicate, object, dataType, graph);
        }
      }
    } else {
      if (dataType == null || dataType.isEmpty()) {
        this.delRelation(cursor, subject, predicate, object, graph);
      } else {
        if (language != null && !language.isEmpty() && dataType.equals(RdfConstants.LANGSTRING)) {
          this.delLanguageTaggedString(cursor, subject, predicate, object, language, graph);
        } else {
          this.delValue(cursor, subject, predicate, object, dataType, graph);
        }
      }
    }
  }

  void start() throws RdfProcessingFailedException;

  void finish() throws RdfProcessingFailedException;
}
