package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

public interface QuadHandler {
  void start() throws LogProcessingFailedException;

  void onPrefix(String prefix, String iri) throws LogProcessingFailedException;

  void onQuad(String subject, String predicate, String object, String valueType, String graph)
      throws LogProcessingFailedException;

  void finish() throws LogProcessingFailedException;
}
