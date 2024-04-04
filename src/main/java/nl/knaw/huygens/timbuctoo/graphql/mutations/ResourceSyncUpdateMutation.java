package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncImport;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantDetermineDataSetException;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.ResourceSyncReport;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class ResourceSyncUpdateMutation extends Mutation<ResourceSyncReport> {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceSyncImportMutation.class);
  private final DataSetRepository dataSetRepository;
  private final ResourceSyncFileLoader resourceSyncFileLoader;

  public ResourceSyncUpdateMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository,
                                    ResourceSyncFileLoader resourceSyncFileLoader) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
    this.resourceSyncFileLoader = resourceSyncFileLoader;
  }

  @Override
  public ResourceSyncReport executeAction(DataFetchingEnvironment env) {
    User user = MutationHelpers.getUser(env);

    String combinedId = env.getArgument("dataSetId");
    // the user-specified authorization token for remote server:
    String authString = env.getArgument("authorization");
    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(combinedId);
    Optional<DataSet> dataSetOpt;

    ResourceSyncReport resourceSyncReport = new ResourceSyncReport();

    try {
      dataSetOpt = dataSetRepository.getDataSet(user, userAndDataSet.left(), userAndDataSet.right());
      if (dataSetOpt.isEmpty()) {
        LOG.error("DataSet does not exist.");
        throw new RuntimeException("DataSet does not exist.");
      }

      DataSet dataSet = dataSetOpt.get();
      MutationHelpers.checkPermission(env, dataSet.getMetadata(), Permission.UPDATE_RESOURCESYNC);

      String capabilityListUri = dataSet.getMetadata().getImportInfo().getFirst().getImportSource();
      Date lastUpdate = dataSet.getMetadata().getImportInfo().getFirst().getLastImportedOn();

      dataSet.getMetadata().getImportInfo().getFirst().setLastImportedOn(Date.from(Instant.now()));
      ResourceSyncMutationFileHelper fileHelper = new ResourceSyncMutationFileHelper(dataSet, resourceSyncReport);

      ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, false);
      resourceSyncImport.filterAndImport(capabilityListUri, null, authString, lastUpdate, fileHelper);
    } catch (IOException | CantRetrieveFileException | CantDetermineDataSetException e) {
      LOG.error("Failed to do a resource sync import. ", e);
      throw new RuntimeException(e);
    }

    return resourceSyncReport;
  }
}
