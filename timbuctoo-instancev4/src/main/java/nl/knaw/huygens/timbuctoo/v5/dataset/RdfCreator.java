package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;

public interface RdfCreator {
  void sendQuads(RdfSerializer saver) throws LogStorageFailedException;
}
