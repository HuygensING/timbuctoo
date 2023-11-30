package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;

public interface OptimizedPatchListener {

  void start() throws RdfProcessingFailedException;

  void onChangedSubject(String subject, ChangeFetcher changeFetcher) throws RdfProcessingFailedException;

  void notifyUpdate();

  void finish() throws RdfProcessingFailedException;
}
