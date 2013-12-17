package nl.knaw.huygens.timbuctoo.persistence;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.rest.model.TestDomainEntity;

import org.junit.Before;
import org.junit.Test;

public class PersistenceWrapperTest {

  private static final Class<TestDomainEntity> DEFAULT_TYPE = TestDomainEntity.class;
  private PersistenceManager persistenceManager;
  private TypeRegistry typeRegistry;

  @Before
  public void setUp() {
    persistenceManager = mock(PersistenceManager.class);
    typeRegistry = mock(TypeRegistry.class);
    when(typeRegistry.getXNameForType(DEFAULT_TYPE)).thenReturn("testconcretedocs");
  }

  private PersistenceWrapper createInstance(String url) {
    return new PersistenceWrapper(url, persistenceManager, typeRegistry);
  }

  @Test
  public void testPersistObjectSucces() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl");
    persistenceWrapper.persistObject(DEFAULT_TYPE, "1234");
    verify(typeRegistry).getXNameForType(DEFAULT_TYPE);
    verify(persistenceManager).persistURL("http://test.nl/" + Paths.DOMAIN_PREFIX + "/testconcretedocs/1234");
  }

  @Test
  public void testPersistObjectWithRevision() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl");
    persistenceWrapper.persistObject(DEFAULT_TYPE, "1234", 12);
    verify(typeRegistry).getXNameForType(DEFAULT_TYPE);
    verify(persistenceManager).persistURL("http://test.nl/" + Paths.DOMAIN_PREFIX + "/testconcretedocs/1234?rev=12");
  }

  @Test
  public void testPersistObjectSuccesUrlEndOnSlash() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl/");
    persistenceWrapper.persistObject(DEFAULT_TYPE, "1234");
    verify(typeRegistry).getXNameForType(DEFAULT_TYPE);
    verify(persistenceManager).persistURL("http://test.nl/" + Paths.DOMAIN_PREFIX + "/testconcretedocs/1234");
  }

  @Test(expected = PersistenceException.class)
  public void testPersistObjectException() throws PersistenceException {
    when(persistenceManager.persistURL(anyString())).thenThrow(new PersistenceException("error"));
    PersistenceWrapper persistenceWrapper = createInstance("http://test.nl/");
    persistenceWrapper.persistObject(DEFAULT_TYPE, "1234");
  }

}
