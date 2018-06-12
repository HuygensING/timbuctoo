package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncImport;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import nl.knaw.huygens.timbuctoo.remote.rs.exceptions.CantDetermineDataSetException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class ResourceSyncUpdateMutation implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceSyncImportMutation.class);
  private final DataSetRepository dataSetRepository;
  private final ResourceSyncFileLoader resourceSyncFileLoader;

  public ResourceSyncUpdateMutation(DataSetRepository dataSetRepository,
                                    ResourceSyncFileLoader resourceSyncFileLoader) {
    this.dataSetRepository = dataSetRepository;
    this.resourceSyncFileLoader = resourceSyncFileLoader;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    User user = MutationHelpers.getUser(env);

    String combinedId = env.getArgument("dataSetId");
    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(combinedId);
    Optional<DataSet> dataSet;

    ResourceSyncImport.ResourceSyncReport resourceSyncReport;

    try {
      dataSet = dataSetRepository.getDataSet(user, userAndDataSet.getLeft(), userAndDataSet.getRight());
      if (!dataSet.isPresent()) {
        LOG.error("DataSet does not exist.");
        throw new RuntimeException("DataSet does not exist.");
      }
      ResourceSyncImport resourceSyncImport = new ResourceSyncImport(
        resourceSyncFileLoader, dataSet.get(), false);
      String capabilityListUri = dataSet.get().getMetadata().getImportSource();
      resourceSyncReport = resourceSyncImport.filterAndImport(capabilityListUri, null, true,
        null);
    } catch (IOException | CantRetrieveFileException | CantDetermineDataSetException e) {
      LOG.error("Failed to do a resource sync import. ", e);
      throw new RuntimeException(e);
    }

    return resourceSyncReport;
  }
}
