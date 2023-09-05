package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;

public class DummyRedirectionServiceFactory implements RedirectionServiceFactory {
  @Override
  public RedirectionService makeRedirectionService(DataSetRepository dataSetRepository) {
    return new DummyRedirectionService(dataSetRepository);
  }
}
