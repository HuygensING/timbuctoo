package nl.knaw.huygens.timbuctoo.core;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.core.dto.ImmutableCreateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimbuctooActionsCreateTest {

  public static final String COLLECTION_NAME = "collectionName";
  private Clock clock;
  private Instant instant;
  private Collection collection;
  private String userId;
  private Optional<Collection> baseCollection;
  private PersistentUrlCreator persistentUrlCreator;
  private DataStoreOperations dataStoreOperations;
  private AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  @Before
  public void setUp() throws Exception {
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION_NAME);
    userId = "userId";
    baseCollection = Optional.empty();
    persistentUrlCreator = mock(PersistentUrlCreator.class);
    dataStoreOperations = mock(DataStoreOperations.class);
    afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
  }

  @Test(expected = PermissionFetchingException.class)
  public void createEntityThrowsAnAuthorizationExceptionWhenTheUserIsNotAllowedToWriteToTheCollection()
    throws Exception {
    TimbuctooActions instance = createInstance(false);

    try {
      instance.createEntity(mock(Collection.class), baseCollection, newArrayList(), "userId");
    } finally {
      verifyZeroInteractions(dataStoreOperations);
    }
  }

  @Test
  public void createEntityLetsDataAccessSaveTheEntity() throws Exception {
    TimbuctooActions instance = createInstance(true);

    UUID id = instance.createEntity(collection, baseCollection, newArrayList(), userId);

    verify(dataStoreOperations).createEntity(
      collection,
      baseCollection,
      ImmutableCreateEntity.builder()
        .properties(newArrayList())
        .id(id)
        .created(new Change(
          instant.toEpochMilli(),
          userId,
          null
        ))
        .build()
    );
  }

  @Test
  public void createEntityReturnsTheId() throws Exception {
    TimbuctooActions instance = createInstance(true);

    UUID id = instance.createEntity(collection, baseCollection, newArrayList(), userId);

    assertThat(id, is(notNullValue()));
  }

  @Test
  public void createEntityNotifiesHandleAdderThatANewEntityIsCreated() throws Exception {
    TimbuctooActions instance = createInstance(true);

    UUID id = instance.createEntity(collection, baseCollection, newArrayList(), userId);

    verify(afterSuccessTaskExecutor).addTask(
      new TimbuctooActions.AddPersistentUrlTask(
        persistentUrlCreator,
        URI.create("http://example.org/persistent"),
        ImmutableEntityLookup.builder().collection(COLLECTION_NAME).timId(id).rev(1).build()
      )
    );
  }

  @Test(expected = IOException.class)
  public void createEntityDoesNotCallTheAfterSuccessTaskExecutor() throws Exception {
    doThrow(IOException.class).when(dataStoreOperations).createEntity(eq(collection), eq(baseCollection), any());
    TimbuctooActions instance = createInstance(true);

    instance.createEntity(collection, baseCollection, newArrayList(), userId);

    verifyZeroInteractions(afterSuccessTaskExecutor);
  }

  private TimbuctooActions createInstance(boolean allowedToWrite) throws PermissionFetchingException {
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);
    if (allowedToWrite) {
      given(permissionFetcher.getPermissions(any(),any())).willReturn(
        Sets.newHashSet(Permission.WRITE, Permission.READ));
    } else {
      given(permissionFetcher.getPermissions(any(),any())).willReturn(
        Sets.newHashSet(Permission.READ));
    }
    return new TimbuctooActions(permissionFetcher, clock, persistentUrlCreator,
      (coll, id, rev) -> URI.create("http://example.org/persistent"), dataStoreOperations, afterSuccessTaskExecutor);
  }

}
