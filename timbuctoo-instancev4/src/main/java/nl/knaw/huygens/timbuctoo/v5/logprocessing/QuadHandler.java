package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

public interface QuadHandler {
  void start(long lineCount) throws LogProcessingFailedException;

  void onPrefix(long line, String prefix, String iri) throws LogProcessingFailedException;

  void onRelation(long line, String subject, String predicate, String object, String graph)
      throws LogProcessingFailedException;

  void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
      throws LogProcessingFailedException;

  void onLanguageTaggedString(long line, String subject, String predicate, String value, String language, String graph)
      throws LogProcessingFailedException;

  default void onQuad(long line, String subject, String predicate, String object, String valueType, String language,
                      String graph) throws LogProcessingFailedException {
    if (valueType == null) {
      this.onRelation(line, subject, predicate, object, graph);
    } else if (RdfConstants.LANGSTRING.equals(valueType)) {
      this.onLanguageTaggedString(line, subject, predicate, object, language, graph);
    } else {
      this.onLiteral(line, subject, predicate, object, valueType, graph);
    }
  }


  void cancel() throws LogProcessingFailedException;

  void finish() throws LogProcessingFailedException;
}
