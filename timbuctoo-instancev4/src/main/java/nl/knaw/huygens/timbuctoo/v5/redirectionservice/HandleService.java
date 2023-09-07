package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.AddTriplePatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.exceptions.RedirectionServiceException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class HandleService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleService.class);
  private static final String PERSISTENT_ID = RdfConstants.timPredicate("persistentUri");
  private final PersistenceManager manager;
  private final DataSetRepository dataSetRepository;

  public HandleService(PersistenceManager manager, DataSetRepository dataSetRepository) {
    this.manager = manager;
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException,
      URISyntaxException, RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = (manager.persistURL(uri.toString()));
    URI persistentUrl = new URI(manager.getPersistentURL(persistentId));

    EntityLookup entityLookup = params.getEntityLookup();
    String dataSetId = entityLookup.getDataSetId();
    Tuple<String, String> ownerIdDataSetId = DataSetMetaData.splitCombinedId(dataSetId);

    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(
        entityLookup.getUser(),
        ownerIdDataSetId.getLeft(),
        ownerIdDataSetId.getRight()
    );

    if (!maybeDataSet.isPresent()) {
      throw new PersistenceException("Can't retrieve DataSet");
    }

    DataSet dataSet = maybeDataSet.get();
    final ImportManager importManager = dataSet.getImportManager();

    try {
      importManager.generateLog(
          dataSet.getMetadata().getBaseUri(), null,
          new AddTriplePatchRdfCreator(
              entityLookup.getUri(),
              PERSISTENT_ID,
              persistentUrl.toString(),
              RdfConstants.STRING
          )
      ).get();
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RedirectionServiceException(e);
    }
  }
}
