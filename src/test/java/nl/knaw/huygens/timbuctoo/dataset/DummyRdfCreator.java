package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.rdfio.RdfSerializer;

import java.util.function.Consumer;

class DummyRdfCreator implements PlainRdfCreator {
  @Override
  public void sendQuads(RdfSerializer saver, DataSet dataSet, Consumer<String> statusConsumer)
    throws LogStorageFailedException {
    saver.onRelation("http://example.com", "http://example.com", "http://example.com", "http://example.com/1");
  }
}
