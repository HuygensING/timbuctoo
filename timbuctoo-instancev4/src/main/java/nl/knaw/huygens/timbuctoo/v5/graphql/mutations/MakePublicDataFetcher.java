package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;

public class MakePublicDataFetcher implements DataFetcher {
  private final DataSetRepository dataSetRepository;

  public MakePublicDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }


  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSet");

    ContextData contextData = env.getContext();

    String userId = contextData.getUser().get().getPersistentId();

    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();

    DataSetMetaData dataSetMetaData = dataSetRepository.publishDataSet(userId, ownerId, dataSetName);

    if (dataSetMetaData != null) {
      return dataSetMetaData;
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }
}
