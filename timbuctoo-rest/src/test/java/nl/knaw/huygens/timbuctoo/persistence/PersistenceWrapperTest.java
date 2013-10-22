package nl.knaw.huygens.timbuctoo.persistence;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;

import org.junit.Before;
import org.junit.Test;

public class PersistenceWrapperTest {

  private PersistenceManager persistenceManager;

  @Before
  public void setUp() {
    persistenceManager = mock(PersistenceManager.class);
  }

  private PersistenceWrapper createInstance(String url) {
    return new PersistenceWrapper(url, persistenceManager);
  }

  @Test
  public void testPersistObjectSucces() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl");
    persistenceWrapper.persistObject("test", "1234");
    verify(persistenceManager).persistURL("http://test.nl/" + Paths.DOMAIN_PREFIX + "/test/1234");
  }

  @Test
  public void testPersistObjectSuccesUrlEndOnSlash() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl/");
    persistenceWrapper.persistObject("test", "1234");
    verify(persistenceManager).persistURL("http://test.nl/" + Paths.DOMAIN_PREFIX + "/test/1234");
  }

  @Test(expected = PersistenceException.class)
  public void testPersistObjectException() throws PersistenceException {
    when(persistenceManager.persistURL(anyString())).thenThrow(new PersistenceException("error"));
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl/");
    persistenceWrapper.persistObject("test", "1234");
  }

}
