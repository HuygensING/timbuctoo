package nl.knaw.huygens.timbuctoo.v5.redirectionservice.bitly;

import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.core.NotFoundException;
import nl.knaw.huygens.timbuctoo.core.TransactionState;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueManager;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.HandleService;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.RedirectionService;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.RedirectionServiceParameters;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.exceptions.RedirectionServiceException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class BitlyService extends RedirectionService {
  public static final String BITLY_QUEUE = "bitly";
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleService.class);
  private static final String PERSISTENT_ID = RdfConstants.timPredicate("persistentUri");
  private final BitlyClient bitlyClient;

  public BitlyService(QueueManager queueManager) {
    super(BITLY_QUEUE, queueManager);
    //TODO: move token to config!
    bitlyClient = new BitlyClient("f349c930b3d6fe64622d83ff44209f44c1eabc1f");
  }

  @Override
  protected void oldSavePid(RedirectionServiceParameters params) throws PersistenceException, URISyntaxException {
    URI uri = params.getUrlToRedirectTo();
    String persistentUrl = retrieveBitlyUri(uri.toString());
    transactionEnforcer.execute(timbuctooActions -> {
        try {
          timbuctooActions.addPid(URI.create(persistentUrl), params.getEntityLookup());
          LOG.info("committed pid");
          return TransactionState.commit();
        } catch (NotFoundException e) {
          LOG.warn("Entity for entityLookup '{}' cannot be found", params.getEntityLookup());
          bitlyClient.userLinkEdit().setArchived(true);

          return TransactionState.rollback();
        }
      }
    );
  }

  @Override
  protected void savePid(RedirectionServiceParameters params)
    throws PersistenceException, URISyntaxException, RedirectionServiceException {
    URI uri = params.getUrlToRedirectTo();
    retrieveBitlyUri(uri.toString());
  }

  public String retrieveBitlyUri(String uri) {
    Response<ShortenResponse> call = bitlyClient.shorten().setLongUrl(uri).call();

    if (call.status_code != 200) {
      return null;
    }
    return call.data.url;
  }
}
