package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.core.NotFoundException;
import nl.knaw.huygens.timbuctoo.core.TransactionState;
import nl.knaw.huygens.timbuctoo.core.dto.EntityLookup;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DummyRedirectionService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RedirectionService.class);
  private static final String PERSISTENT_ID = RdfConstants.timPredicate("persistentUri");
  private final DataSetRepository dataSetRepository;

  public DummyRedirectionService(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
    LOG.info("Using dummy persistence manager instead of real server");
  }

  @Override
  protected void oldSavePid(RedirectionServiceParameters params) throws URISyntaxException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = uri.toString();
    URI persistentUrl = new URI("http://example.org/persistentid#" +
        URLEncoder.encode(persistentId, StandardCharsets.UTF_8));

    transactionEnforcer.execute(timbuctooActions -> {
        try {
          timbuctooActions.addPid(persistentUrl, params.getEntityLookup());
          LOG.info("committed pid");
          return TransactionState.commit();
        } catch (NotFoundException e) {
          LOG.warn("Entity for entityLookup '{}' cannot be found", params.getEntityLookup());
          return TransactionState.rollback();
        }
      }
    );
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException, URISyntaxException,
      RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = uri.toString();
    URI persistentUrl = new URI("http://example.org/persistentid#" +
        URLEncoder.encode(persistentId, StandardCharsets.UTF_8));

    EntityLookup entityLookup = params.getEntityLookup();

    String dataSetId = entityLookup.getDataSetId().get();

    Tuple<String, String> ownerIdDataSetId = DataSetMetaData.splitCombinedId(dataSetId);

    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(
        entityLookup.getUser().get(),
        ownerIdDataSetId.getLeft(),
        ownerIdDataSetId.getRight());

    if (!maybeDataSet.isPresent()) {
      throw new PersistenceException("Can't retrieve DataSet");
    }

    DataSet dataSet = maybeDataSet.get();

    final ImportManager importManager = dataSet.getImportManager();

    try {
      importManager.generateLog(
          dataSet.getMetadata().getBaseUri(), null,
          new AddTriplePatchRdfCreator(
              entityLookup.getUri().get(),
              PERSISTENT_ID,
              persistentUrl.toString(),
              RdfConstants.STRING)
      ).get();
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RedirectionServiceException(e);
    }
  }
}
