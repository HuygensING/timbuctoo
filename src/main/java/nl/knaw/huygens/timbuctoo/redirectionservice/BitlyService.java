package nl.knaw.huygens.timbuctoo.redirectionservice;

import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.redirectionservice.exceptions.RedirectionServiceException;
import org.slf4j.Logger;

import java.net.URI;

public class BitlyService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BitlyService.class);
  private final BitlyClient bitlyClient;

  public BitlyService(DataSetRepository dataSetRepository, String accessToken) {
    super(dataSetRepository);
    bitlyClient = new BitlyClient(accessToken);
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException,
      RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentUrl = retrieveBitlyUri(uri.toString());
    registerPid(params, persistentUrl);
  }

  public String retrieveBitlyUri(String uri) {
    Response<ShortenResponse> call = bitlyClient.shorten().setLongUrl(uri).call();
    if (call.status_code != 200) {
      return null;
    }
    return call.data.url;
  }
}
