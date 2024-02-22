package nl.knaw.huygens.timbuctoo.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.redirectionservice.exceptions.RedirectionServiceException;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class HandleService extends RedirectionService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleService.class);
  private final PersistenceManager manager;

  public HandleService(PersistenceManager manager, DataSetRepository dataSetRepository) {
    super(dataSetRepository);
    this.manager = manager;
  }

  @Override
  protected void savePid(RedirectionServiceParameters params) throws PersistenceException,
      URISyntaxException, RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    LOG.info(String.format("Retrieving persistent url for '%s'", uri));
    String persistentId = (manager.persistURL(uri.toString()));
    URI persistentUrl = new URI(manager.getPersistentURL(persistentId));
    registerPid(params, persistentUrl.toString());
  }
}
