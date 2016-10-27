package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
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
  }

  @Test
  public void deleteEntityLetsDataAccessDeleteTheEntity() throws Exception {
    when(transactionEnforcer.deleteEntity(collection, ID, change)).thenReturn(DeleteMessage.success());
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.deleteEntity(collection, ID, USER_ID);

    verify(transactionEnforcer).deleteEntity(collection, ID, change);
  }

  @Test(expected = AuthorizationException.class)
  public void deleteEntityThrowsAnUnAuthrozedExceptionIfTheUserIsNotAllowedToWriteTheCollection() throws Exception {
    TimbuctooActions instance = new TimbuctooActions(notAllowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    try {
      instance.deleteEntity(collection, ID, USER_ID);
    } finally {
      verifyZeroInteractions(transactionEnforcer);
    }
  }

  @Test(expected = NotFoundException.class)
  public void deleteEntityThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    when(transactionEnforcer.deleteEntity(collection, ID, change)).thenReturn(DeleteMessage.notFound());
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.deleteEntity(collection, ID, USER_ID);
  }


}
