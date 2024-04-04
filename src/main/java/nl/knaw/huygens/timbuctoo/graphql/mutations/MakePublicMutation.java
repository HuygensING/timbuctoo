package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakePublicMutation extends Mutation<DataSetWithDatabase> {
  private static final Logger LOG = LoggerFactory.getLogger(MakePublicMutation.class);
  private final DataSetRepository dataSetRepository;

  public MakePublicMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public DataSetWithDatabase executeAction(DataFetchingEnvironment env) {
    User user = MutationHelpers.getUser(env);
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    UserPermissionCheck userPermissionCheck = env.getGraphQlContext().get("userPermissionCheck");

    try {
      dataSetRepository.publishDataSet(user, dataSet.getMetadata().getOwnerId(), dataSet.getMetadata().getDataSetId());
    } catch (DataSetPublishException e) {
      LOG.error("Failed to publish data set", e);
      throw new RuntimeException("Failed to publish data set");
    }

    return new DataSetWithDatabase(dataSet, userPermissionCheck);
  }
}
