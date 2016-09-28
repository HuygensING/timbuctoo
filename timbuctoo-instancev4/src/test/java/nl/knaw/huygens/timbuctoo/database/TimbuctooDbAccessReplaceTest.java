package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimbuctooDbAccessReplaceTest {

  public static final int NEW_REV = 1;
  public static final String COLLECTION_NAME = "collection";
  public static final UUID ID = UUID.randomUUID();
  private static final String USER_ID = "userId";
  private DataAccess dataAccess;
  private DbUpdateEntity dbUpdateEntity;
  private Clock clock;
  private HandleAdder handleAdder;
  private UpdateEntity updateEntity;
  private Collection collection;
  private Instant instant;

  @Before
  public void setUp() throws Exception {
    dataAccess = mock(DataAccess.class);
    dbUpdateEntity = mock(DbUpdateEntity.class);
    when(dataAccess.updateEntity(any(Collection.class), any(UpdateEntity.class))).thenReturn(dbUpdateEntity);
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    handleAdder = mock(HandleAdder.class);
    updateEntity = mock(UpdateEntity.class);
    when(updateEntity.getId()).thenReturn(ID);
    collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION_NAME);
  }

  @Test(expected = AuthorizationException.class)
  public void replaceEntityThrowsAnUnauthorizedExceptionWhenTheUserIsNotAllowedToWrite() throws Exception {
    TimbuctooDbAccess instance = new TimbuctooDbAccess(notAllowedToWrite(), dataAccess, clock, handleAdder);

    instance.replaceEntity(collection, updateEntity, USER_ID);
  }

  @Test
  public void replaceEntityAddsAHandleAfterASuccessfulUpdate() throws Exception {
    when(dataAccess.executeAndReturn(dbUpdateEntity)).thenReturn(UpdateReturnMessage.success(NEW_REV));
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.replaceEntity(collection, updateEntity, USER_ID);

    InOrder inOrder = inOrder(dataAccess, handleAdder);
    inOrder.verify(dataAccess).updateEntity(collection, updateEntity);
    inOrder.verify(dataAccess).executeAndReturn(dbUpdateEntity);
    inOrder.verify(handleAdder).add(new HandleAdderParameters(COLLECTION_NAME, ID, NEW_REV));

  }

  @Test
  public void replaceEntityAddsTheModifiedPropertyToUpdateEntityBeforeExecutingTheUpdate() throws Exception {
    when(dataAccess.executeAndReturn(dbUpdateEntity)).thenReturn(UpdateReturnMessage.success(NEW_REV));
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.replaceEntity(collection, updateEntity, USER_ID);

    InOrder inOrder = inOrder(dataAccess, updateEntity);
    inOrder.verify(updateEntity).setModified(argThat(allOf(
      hasProperty("timeStamp", is(instant.toEpochMilli())),
      hasProperty("userId", is(USER_ID))
    )));
    inOrder.verify(dataAccess).updateEntity(collection, updateEntity);
  }

  @Test(expected = NotFoundException.class)
  public void replaceEntityThrowsANotFoundExceptionWhenExecuteAndReturnReturnsAnUpdateStatusNotFound()
    throws Exception {
    when(dataAccess.executeAndReturn(dbUpdateEntity)).thenReturn(UpdateReturnMessage.notFound());
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.replaceEntity(collection, updateEntity, USER_ID);
  }

  @Test(expected = AlreadyUpdatedException.class)
  public void replaceEntityThrowsAnAlreadyUpdatedExceptionWhenExecuteAndReturnReturnsAnUpdateStatusAlreadyUpdated()
    throws Exception {
    when(dataAccess.executeAndReturn(dbUpdateEntity)).thenReturn(UpdateReturnMessage.allreadyUpdated());
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    instance.replaceEntity(collection, updateEntity, USER_ID);
  }

}
