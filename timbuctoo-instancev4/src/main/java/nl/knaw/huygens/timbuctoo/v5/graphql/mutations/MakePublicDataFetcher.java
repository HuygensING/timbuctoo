package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakePublicDataFetcher implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(MakePublicDataFetcher.class);
  private final DataSetRepository dataSetRepository;

  public MakePublicDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }


  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSetId");

    ContextData contextData = env.getContext();

    User user = contextData.getUser().get();

    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();


    try {
      dataSetRepository.publishDataSet(user, ownerId, dataSetName);
    } catch (DataSetPublishException e) {
      LOG.error("Failed to publish data set", e);
      throw new RuntimeException("Failed to publish data set");
    }

    DataSetMetaData dataSetMetaData = dataSetRepository.getDataSet(user, ownerId, dataSetName).get().getMetadata();

    if (dataSetMetaData != null) {
      return dataSetMetaData;
    } else {
      throw new RuntimeException("Data set does not exist");
    }
  }
}
