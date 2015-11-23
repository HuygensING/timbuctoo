package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ModPersisterTest {

  public static final ProjectADomainEntity DOMAIN_ENTITY = new ProjectADomainEntity();
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final int A_MILLISECOND = 1;

  private PersistenceWrapper persistenceWrapper;
  private ModPersister instance;

  @Before
  public void setUp() throws Exception {
    persistenceWrapper = mock(PersistenceWrapper.class);
    instance = new ModPersister(persistenceWrapper, A_MILLISECOND);
  }

  @Test
  public void executeUpdatesThePIDWithTheNewReferenceOfTheDomainEntity() throws Exception {
    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(1)).updatePID(DOMAIN_ENTITY);
  }

  @Test
  public void executeTriesFiveTimeAtMostWhenThePIDCannotBeUpdated() throws Exception {
    // setup
    doThrow(PersistenceException.class).when(persistenceWrapper).updatePID(DOMAIN_ENTITY);

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(5)).updatePID(DOMAIN_ENTITY);
  }
}
