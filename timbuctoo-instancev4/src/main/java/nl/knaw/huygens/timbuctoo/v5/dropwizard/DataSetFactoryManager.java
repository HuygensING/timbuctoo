package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;

public class DataSetFactoryManager implements Managed {
  private final DataSetFactory dataSetFactory;

  public DataSetFactoryManager(DataSetFactory dataSetFactory) {
    this.dataSetFactory = dataSetFactory;
  }

  @Override
  public void start() throws Exception {
    dataSetFactory.start();
  }

  @Override
  public void stop() throws Exception {
    dataSetFactory.stop();
  }
}
