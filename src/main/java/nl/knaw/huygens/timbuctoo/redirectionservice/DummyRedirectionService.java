package nl.knaw.huygens.timbuctoo.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.redirectionservice.exceptions.RedirectionServiceException;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DummyRedirectionService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RedirectionService.class);

  public DummyRedirectionService(DataSetRepository dataSetRepository) {
    super(dataSetRepository);
    LOG.info("Using dummy persistence manager instead of real server");
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException, URISyntaxException,
      RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = uri.toString();
    URI persistentUrl = new URI("http://example.org/persistentid#" +
        URLEncoder.encode(persistentId, StandardCharsets.UTF_8));
    registerPid(params, persistentUrl.toString());
  }
}
