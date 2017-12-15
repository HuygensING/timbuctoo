package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.NotEnoughPermissionsException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.RootData;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class DeleteDataSetDataFetcher implements DataFetcher {


  private static final Logger LOG = LoggerFactory.getLogger(DeleteDataSetDataFetcher.class);
  private final DataSetRepository dataSetRepository;

  public DeleteDataSetDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    Optional<User> currentUser = ((RootData) environment.getRoot()).getCurrentUser();
    if (!currentUser.isPresent()) {
      throw new RuntimeException("User is not provided");
    }

    String dataSetId = environment.getArgument("dataSetId");

    Tuple<String, String> ownerDataSetName = DataSetMetaData.splitCombinedId(dataSetId);

    try {
      dataSetRepository.removeDataSet(ownerDataSetName.getLeft(),ownerDataSetName.getRight(),currentUser.get());

      return new RemovedDataSet(dataSetId);
    } catch (IOException | NotEnoughPermissionsException e) {
      LOG.error("Data set deletion exception", e);
      throw new RuntimeException("Data set could not be deleted");
    }
  }

  public static class RemovedDataSet {
    private final String dataSetId;

    public RemovedDataSet(String dataSetId) {
      this.dataSetId = dataSetId;
    }

    public String getDataSetId() {
      return dataSetId;
    }
  }
}
