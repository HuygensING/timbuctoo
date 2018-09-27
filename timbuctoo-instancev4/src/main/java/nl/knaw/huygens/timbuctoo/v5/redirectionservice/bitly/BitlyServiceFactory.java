package nl.knaw.huygens.timbuctoo.v5.redirectionservice.bitly;

import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueManager;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.RedirectionService;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.RedirectionServiceFactory;

public class BitlyServiceFactory implements RedirectionServiceFactory {
  @Override
  public RedirectionService makeRedirectionService(QueueManager queueManager, DataSetRepository dataSetRepository) {
    return new BitlyService(queueManager);
  }
}
