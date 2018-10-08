package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import org.apache.commons.lang.NotImplementedException;

import java.net.URLEncoder;

public class DummyPersistenceManager implements PersistenceManager {
  @Override
  public String persistURL(String url) throws PersistenceException {
    return url;
  }

  @Override
  public String getPersistedURL(String persistentId) throws PersistenceException {
    throw new NotImplementedException(
      "This method is not implemented on the dummy. You probably meant getPersistentURL() btw."
    );
  }

  @Override
  public String getPersistentURL(String persistentId) {
    return "http://example.org/persistentid#" + URLEncoder.encode(persistentId);
  }

  @Override
  public void deletePersistentId(String persistentId) throws PersistenceException {
  }

  @Override
  public void modifyURLForPersistentId(String persistentId, String url) throws PersistenceException {
  }
}
