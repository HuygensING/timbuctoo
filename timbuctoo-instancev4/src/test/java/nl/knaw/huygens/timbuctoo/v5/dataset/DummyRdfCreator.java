package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;

class DummyRdfCreator implements RdfCreator {
  @Override
  public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {
    saver.onRelation("http://example.com", "http://example.com", "http://example.com", "http://example.com/1");
  }
}
