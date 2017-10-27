package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ViewConfigDataFetcher implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(ViewConfigDataFetcher.class);
  private final DataSetRepository dataSetRepository;

  public ViewConfigDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSet");
    String collectionUri = env.getArgument("collectionUri");
    Object viewConfig = env.getArgument("viewConfig");

    Tuple<String, String> userAndDataSet = PromotedDataSet.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();
    if (dataSetRepository.dataSetExists(ownerId, dataSetName)) {
      DataSet dataSet = dataSetRepository.getDataSet(ownerId, dataSetName).get();
      dataSet.getQuadStore();
      try {
        final String baseUri = dataSet.getMetadata().getBaseUri();
        dataSet.getImportManager().generateLog(
          baseUri,
          baseUri,
          new GraphQlPatchRdfCreator(dataSet.getQuadStore(), collectionUri, viewConfig, baseUri)
        ).get();
        return viewConfig;
      } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
        LOG.error("Could not store the view config", e);
      }
    }

    return new ArrayList();

  }

}
