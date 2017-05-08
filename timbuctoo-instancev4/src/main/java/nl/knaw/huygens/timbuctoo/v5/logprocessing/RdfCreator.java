package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;

public interface RdfCreator {
  void sendQuads(QuadSaver saver) throws LogStorageFailedException;
}
