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

public class TimbuctooDbAccessDeleteTest {

  public static final String USER_ID = "userId";
  public static final UUID ID = UUID.randomUUID();
  private DataAccess dataAccess;
  private Clock clock;
  private HandleAdder handleAdder;
  private Collection collection;
  private Instant instant;
  private Change change;

  @Before
  public void setUp() throws Exception {
    dataAccess = mock(DataAccess.class);
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
    when(dataAccess.deleteEntity(collection, ID, change)).thenReturn(DeleteMessage.success());
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.deleteEntity(collection, ID, USER_ID);

    verify(dataAccess).deleteEntity(collection, ID, change);
  }

  @Test(expected = AuthorizationException.class)
  public void deleteEntityThrowsAnUnAuthrozedExceptionIfTheUserIsNotAllowedToWriteTheCollection() throws Exception {
    TimbuctooDbAccess instance = new TimbuctooDbAccess(notAllowedToWrite(), dataAccess, clock, handleAdder);

    try {
      instance.deleteEntity(collection, ID, USER_ID);
    } finally {
      verifyZeroInteractions(dataAccess);
    }
  }

  @Test(expected = NotFoundException.class)
  public void deleteEntityThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    when(dataAccess.deleteEntity(collection, ID, change)).thenReturn(DeleteMessage.notFound());
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.deleteEntity(collection, ID, USER_ID);
  }


}
