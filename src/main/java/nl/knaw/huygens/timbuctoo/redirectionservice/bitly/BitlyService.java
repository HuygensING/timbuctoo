package nl.knaw.huygens.timbuctoo.redirectionservice.bitly;

import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.dataset.AddTriplePatchRdfCreator;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.redirectionservice.EntityLookup;
import nl.knaw.huygens.timbuctoo.redirectionservice.exceptions.RedirectionServiceException;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionService;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionServiceParameters;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class BitlyService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BitlyService.class);
  private static final String PERSISTENT_ID = RdfConstants.timPredicate("persistentUri");
  private final BitlyClient bitlyClient;
  private final DataSetRepository dataSetRepository;

  public BitlyService(DataSetRepository dataSetRepository, String accessToken) {
    this.dataSetRepository = dataSetRepository;
    bitlyClient = new BitlyClient(accessToken);
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException,
      RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentUrl = retrieveBitlyUri(uri.toString());

    EntityLookup entityLookup = params.getEntityLookup();
    String dataSetId = entityLookup.getDataSetId();
    Tuple<String, String> ownerIdDataSetId = DataSetMetaData.splitCombinedId(dataSetId);

    Optional<DataSet> maybeDataSet = dataSetRepository.getDataSet(
      entityLookup.getUser(),
      ownerIdDataSetId.left(),
      ownerIdDataSetId.right());

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
          RdfConstants.STRING)
      ).get();
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RedirectionServiceException(e);
    }
  }

  public String retrieveBitlyUri(String uri) {
    Response<ShortenResponse> call = bitlyClient.shorten().setLongUrl(uri).call();
    if (call.status_code != 200) {
      return null;
    }
    return call.data.url;
  }
}
