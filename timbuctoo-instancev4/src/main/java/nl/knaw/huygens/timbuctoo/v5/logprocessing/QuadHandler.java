package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

public interface QuadHandler {
  void start(long lineCount) throws LogProcessingFailedException;

  void onPrefix(long line, String prefix, String iri) throws LogProcessingFailedException;

  void onRelation(long line, String subject, String predicate, String object, String graph)
    throws LogProcessingFailedException;

  void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
    throws LogProcessingFailedException;

  void onLanguageTaggedString(long line, String subject, String predicate, String value, String language, String graph)
    throws LogProcessingFailedException;

  void cancel() throws LogProcessingFailedException;

  void finish() throws LogProcessingFailedException;
}
