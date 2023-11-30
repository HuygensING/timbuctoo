package nl.knaw.huygens.timbuctoo.redirectionservice;

import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;

public class DummyRedirectionServiceFactory implements RedirectionServiceFactory {
  @Override
  public RedirectionService makeRedirectionService(DataSetRepository dataSetRepository) {
    return new DummyRedirectionService(dataSetRepository);
  }
}
