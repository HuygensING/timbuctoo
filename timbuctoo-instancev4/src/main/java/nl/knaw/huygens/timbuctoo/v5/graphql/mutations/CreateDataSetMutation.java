package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.MutationHelpers.getUser;

public class CreateDataSetMutation implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(CreateDataSetMutation.class);

  private final DataSetRepository dataSetRepository;

  public CreateDataSetMutation(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    User currentUser = getUser(environment);

    String dataSetName = environment.getArgument("dataSetName");
    try {
      return new DataSetWithDatabase(dataSetRepository.createDataSet(currentUser, dataSetName, ""));
    } catch (DataStoreCreationException e) {
      LOG.error("Data set creation exception", e);
      throw new RuntimeException("Data set could not be created");
    } catch (IllegalDataSetNameException e) {
      throw new RuntimeException("Data set id is not supported: " + e.getMessage());
    }
  }
}
