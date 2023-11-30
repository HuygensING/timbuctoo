package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.rdfio.RdfSerializer;

import java.util.function.Consumer;

public interface PlainRdfCreator extends RdfCreator {
  void sendQuads(RdfSerializer saver, DataSet dataSet, Consumer<String> statusConsumer)
    throws LogStorageFailedException;
}
