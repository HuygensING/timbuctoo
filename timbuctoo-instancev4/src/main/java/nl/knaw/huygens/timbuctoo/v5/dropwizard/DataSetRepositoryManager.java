package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;

public class DataSetRepositoryManager implements Managed {
  private final DataSetRepository dataSetRepository;
  private final DataSetConfiguration dataSetConfiguration;
  private final String authorizationsPathForMigration;
  private final UserValidator userValidator;

  public DataSetRepositoryManager(DataSetRepository dataSetRepository,
                                  DataSetConfiguration dataSetConfiguration,
                                  String authorizationsPathForMigration,
                                  UserValidator userValidator) {
    this.dataSetRepository = dataSetRepository;
    this.dataSetConfiguration = dataSetConfiguration;
    this.authorizationsPathForMigration = authorizationsPathForMigration;
    this.userValidator = userValidator;
  }

  @Override
  public void start() throws Exception {
    new MetaDataMigration(dataSetConfiguration).migrate();
    new AuthorizationMigration(
      authorizationsPathForMigration,
      userValidator,
      dataSetConfiguration.getDataSetMetadataLocation()
    ).migrate();
    dataSetRepository.start();
  }

  @Override
  public void stop() throws Exception {
    dataSetRepository.stop();
  }
}
