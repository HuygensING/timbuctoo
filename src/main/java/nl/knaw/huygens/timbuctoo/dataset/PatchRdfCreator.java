package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.rdfio.RdfPatchSerializer;

import java.util.function.Consumer;

public interface PatchRdfCreator extends RdfCreator {
  void sendQuads(RdfPatchSerializer saver, Consumer<String> importStatusConsumer, DataSet dataSet)
    throws LogStorageFailedException;
}
