package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;

public interface QuadGenerator {
  void sendQuads(QuadHandler handler) throws LogStorageFailedException;
}
