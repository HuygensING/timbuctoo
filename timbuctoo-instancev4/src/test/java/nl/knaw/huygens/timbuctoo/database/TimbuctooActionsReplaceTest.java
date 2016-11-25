package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
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
  private Clock clock;
  private PersistentUrlCreator persistentUrlCreator;
  private UpdateEntity updateEntity;
  private Collection collection;
  private Instant instant;
  private AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  @Before
  public void setUp() throws Exception {
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    persistentUrlCreator = mock(PersistentUrlCreator.class);
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

    InOrder inOrder = inOrder(dataStoreOperations, persistentUrlCreator, afterSuccessTaskExecutor);
    inOrder.verify(dataStoreOperations).replaceEntity(collection, updateEntity);
    inOrder.verify(afterSuccessTaskExecutor).addTask(
      new TimbuctooActions.AddPersistentUrlTask(
        persistentUrlCreator,
        URI.create("http://example.org/persistent"),
        ImmutableEntityLookup.builder().collection(COLLECTION_NAME).timId(ID).rev(NEW_REV).build()
      )
    );

  }

  @Test
  public void replaceEntityAddsTheModifiedPropertyToUpdateEntityBeforeExecutingTheUpdate() throws Exception {
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
    return new TimbuctooActions(authorizer, clock, persistentUrlCreator,
      (coll, id, rev) -> URI.create("http://example.org/persistent"), dataStoreOperations, afterSuccessTaskExecutor);
  }

}
