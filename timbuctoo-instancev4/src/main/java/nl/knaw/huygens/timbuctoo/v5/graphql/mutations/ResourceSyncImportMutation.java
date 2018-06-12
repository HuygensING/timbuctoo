package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncImport;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import nl.knaw.huygens.timbuctoo.remote.rs.exceptions.CantDetermineDataSetException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class ResourceSyncImportMutation implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceSyncImportMutation.class);
  private final DataSetRepository dataSetRepository;
  private final ResourceSyncFileLoader resourceSyncFileLoader;

  public ResourceSyncImportMutation(DataSetRepository dataSetRepository,
                                    ResourceSyncFileLoader resourceSyncFileLoader) {
    this.dataSetRepository = dataSetRepository;
    this.resourceSyncFileLoader = resourceSyncFileLoader;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    User user = MutationHelpers.getUser(env);

    String dataSetName = env.getArgument("dataSetName");
    String capabilityListUri = env.getArgument("capabilityListUri");
    String userSpecifiedDataSet = env.getArgument("userSpecifiedDataSet");
    String authString = env.getArgument("authorization");
    DataSet dataSet;

    ResourceSyncImport.ResourceSyncReport resourceSyncReport;

    try {
      dataSet = dataSetRepository.createDataSet(user, dataSetName, capabilityListUri);
      ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, dataSet, false);
      resourceSyncReport = resourceSyncImport.filterAndImport(capabilityListUri, userSpecifiedDataSet,
        false, authString);
    } catch (DataStoreCreationException | IllegalDataSetNameException | IOException |
      CantRetrieveFileException | CantDetermineDataSetException e) {
      LOG.error("Failed to do a resource sync import. ", e);
      throw new RuntimeException(e);
    }

    return resourceSyncReport;
  }
}
