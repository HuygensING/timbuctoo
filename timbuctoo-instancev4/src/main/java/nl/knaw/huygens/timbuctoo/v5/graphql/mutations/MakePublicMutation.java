package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakePublicMutation implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(MakePublicMutation.class);
  private final DataSetRepository dataSetRepository;

  public MakePublicMutation(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }


  @Override
  public Object get(DataFetchingEnvironment env) {
    User user = MutationHelpers.getUser(env);

    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);

    try {
      dataSetRepository.publishDataSet(user, dataSet.getMetadata().getOwnerId(), dataSet.getMetadata().getDataSetId());
    } catch (DataSetPublishException e) {
      LOG.error("Failed to publish data set", e);
      throw new RuntimeException("Failed to publish data set");
    }
    return new DataSetWithDatabase(dataSet);
  }
}
