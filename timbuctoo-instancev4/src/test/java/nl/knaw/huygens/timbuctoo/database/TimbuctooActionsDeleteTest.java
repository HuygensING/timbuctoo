package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimbuctooActionsDeleteTest {

  public static final String USER_ID = "userId";
  public static final UUID ID = UUID.randomUUID();
  private TransactionEnforcer transactionEnforcer;
  private Clock clock;
  private HandleAdder handleAdder;
  private Collection collection;
  private Instant instant;
  private Change change;
  private DataStoreOperations dataStoreOperations;

  @Before
  public void setUp() throws Exception {
    transactionEnforcer = mock(TransactionEnforcer.class);
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    handleAdder = mock(HandleAdder.class);
    collection = mock(Collection.class);
    change = new Change();
    change.setUserId(USER_ID);
    change.setTimeStamp(instant.toEpochMilli());
    dataStoreOperations = mock(DataStoreOperations.class);
  }

  @Test
  public void deleteEntityLetsDataAccessDeleteTheEntity() throws Exception {
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.deleteEntity(collection, ID, USER_ID);

    verify(dataStoreOperations).deleteEntity(collection, ID, change);
  }

  @Test(expected = AuthorizationException.class)
  public void deleteEntityThrowsAnUnAuthrozedExceptionIfTheUserIsNotAllowedToWriteTheCollection() throws Exception {
    TimbuctooActions instance = createInstance(notAllowedToWrite());

    try {
      instance.deleteEntity(collection, ID, USER_ID);
    } finally {
      verifyZeroInteractions(dataStoreOperations);
    }
  }

  @Test(expected = NotFoundException.class)
  public void deleteEntityThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    doThrow(new NotFoundException()).when(dataStoreOperations).deleteEntity(collection, ID, change);
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.deleteEntity(collection, ID, USER_ID);
  }

  private TimbuctooActions createInstance(Authorizer authorizer) throws AuthorizationUnavailableException {
    return new TimbuctooActions(authorizer, transactionEnforcer, clock, handleAdder,
      dataStoreOperations, null);
  }


}
