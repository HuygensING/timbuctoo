package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

public interface QuadLoader {
  void sendQuads(QuadHandler handler) throws LogProcessingFailedException;
}
