package nl.knaw.huygens.timbuctoo.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.dataset.AddTriplePatchRdfCreator;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.redirectionservice.exceptions.RedirectionServiceException;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class RedirectionService {
  private static final String PERSISTENT_ID = RdfConstants.timPredicate("persistentUri");
  private static final Logger LOG = LoggerFactory.getLogger(RedirectionService.class);

  private final DataSetRepository dataSetRepository;

  protected RedirectionService(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  private void actualExecution(RedirectionServiceParameters params) {
    try {
      savePid(params);
    } catch (PersistenceException | URISyntaxException | RedirectionServiceException e) {
      LOG.error("Could not create handle", e);
      if (params.getRetries() < 5) {
        LOG.warn(String.format("Re-adding %s%s job to the queue for '%s' '%s'",
            params.getRetries() + 1, getOrdinalSuffix(params.getRetries() + 1),
            params.getEntityLookup(),
            params.getUrlToRedirectTo()
        ));
        this.actualExecution(params.nextTry());
      }
    }
  }

  // gogo gadgetstackoverflow
  private String getOrdinalSuffix(int value) {
    int hunRem = value % 100;
    int tenRem = value % 10;

    if (hunRem - tenRem == 10) {
      return "th";
    }
    return switch (tenRem) {
      case 1 -> "st";
      case 2 -> "nd";
      case 3 -> "rd";
      default -> "th";
    };
  }

  protected void registerPid(RedirectionServiceParameters params, String persistentUrl)
      throws PersistenceException, RedirectionServiceException {
    EntityLookup entityLookup = params.getEntityLookup();
    String dataSetId = entityLookup.getDataSetId();
    Tuple<String, String> ownerIdDataSetId = DataSetMetaData.splitCombinedId(dataSetId);

    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(
        entityLookup.getUser(),
        ownerIdDataSetId.left(),
        ownerIdDataSetId.right()
    );

    if (maybeDataSet.isEmpty()) {
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
              persistentUrl,
              RdfConstants.STRING
          )
      ).get();
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RedirectionServiceException(e);
    }
  }

  protected abstract void savePid(RedirectionServiceParameters params) throws
      PersistenceException, URISyntaxException, RedirectionServiceException;

  public final void add(URI uriToRedirectTo, EntityLookup entityLookup) {
    this.actualExecution(new RedirectionServiceParameters(uriToRedirectTo, entityLookup));
  }
}
