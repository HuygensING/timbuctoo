package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimbuctooActionsReplaceTest {

  public static final int NEW_REV = 1;
  public static final String COLLECTION_NAME = "collection";
  public static final UUID ID = UUID.randomUUID();
  private static final String USER_ID = "userId";
  private final DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
  private TransactionEnforcer transactionEnforcer;
  private Clock clock;
  private HandleAdder handleAdder;
  private UpdateEntity updateEntity;
  private Collection collection;
  private Instant instant;
  private AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  @Before
  public void setUp() throws Exception {
    transactionEnforcer = mock(TransactionEnforcer.class);
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    handleAdder = mock(HandleAdder.class);
    updateEntity = mock(UpdateEntity.class);
    when(updateEntity.getId()).thenReturn(ID);
    collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION_NAME);
    afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
  }

  @Test(expected = AuthorizationException.class)
  public void replaceEntityThrowsAnUnauthorizedExceptionWhenTheUserIsNotAllowedToWrite() throws Exception {
    TimbuctooActions instance = createInstance(notAllowedToWrite());

    try {
      instance.replaceEntity(collection, updateEntity, USER_ID);
    } finally {
      verifyZeroInteractions(dataStoreOperations);
    }
  }

  @Test
  public void replaceEntityAddsAHandleAfterASuccessfulUpdate() throws Exception {
    when(dataStoreOperations.replaceEntity(collection, updateEntity)).thenReturn(NEW_REV);
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceEntity(collection, updateEntity, USER_ID);

    InOrder inOrder = inOrder(dataStoreOperations, handleAdder, afterSuccessTaskExecutor);
    inOrder.verify(dataStoreOperations).replaceEntity(collection, updateEntity);
    inOrder.verify(afterSuccessTaskExecutor).addHandleTask(
      handleAdder,
      new HandleAdderParameters(COLLECTION_NAME, ID, NEW_REV)
    );
    // inOrder.verify(handleAdder).add(new HandleAdderParameters(COLLECTION_NAME, ID, NEW_REV));

  }

  @Test
  public void replaceEntityAddsTheModifiedPropertyToUpdateEntityBeforeExecutingTheUpdate() throws Exception {
    when(transactionEnforcer.updateEntity(collection, updateEntity)).thenReturn(UpdateReturnMessage.success(NEW_REV));
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceEntity(collection, updateEntity, USER_ID);

    InOrder inOrder = inOrder(dataStoreOperations, updateEntity);
    inOrder.verify(updateEntity).setModified(argThat(allOf(
      hasProperty("timeStamp", is(instant.toEpochMilli())),
      hasProperty("userId", is(USER_ID))
    )));
    inOrder.verify(dataStoreOperations).replaceEntity(collection, updateEntity);
  }

  @Test(expected = NotFoundException.class)
  public void replaceEntityThrowsANotFoundExceptionWhenExecuteAndReturnReturnsAnUpdateStatusNotFound()
    throws Exception {
    when(dataStoreOperations.replaceEntity(collection, updateEntity)).thenThrow(new NotFoundException());
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceEntity(collection, updateEntity, USER_ID);
  }

  @Test(expected = AlreadyUpdatedException.class)
  public void replaceEntityThrowsAnAlreadyUpdatedExceptionWhenExecuteAndReturnReturnsAnUpdateStatusAlreadyUpdated()
    throws Exception {
    when(dataStoreOperations.replaceEntity(collection, updateEntity)).thenThrow(new AlreadyUpdatedException());
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceEntity(collection, updateEntity, USER_ID);
  }

  private TimbuctooActions createInstance(Authorizer authorizer) throws AuthorizationUnavailableException {
    return new TimbuctooActions(authorizer, transactionEnforcer, clock, handleAdder,
      dataStoreOperations, afterSuccessTaskExecutor);
  }

}
