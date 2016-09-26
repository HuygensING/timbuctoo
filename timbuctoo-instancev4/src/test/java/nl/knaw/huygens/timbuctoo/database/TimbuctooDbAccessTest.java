package nl.knaw.huygens.timbuctoo.database;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TimbuctooDbAccessTest {

  public static final String COLLECTION_NAME = "collectionName";
  private DataAccess dataAccess;
  private Clock clock;
  private Instant instant;
  private Collection collection;
  private CreateEntity entity;
  private String userId;
  private Optional<Collection> baseCollection;
  private HandleAdder handleAdder;
  private DbCreateEntity dbCreateEntity;
  private TransactionState transactionState;

  @Before
  public void setUp() throws Exception {
    dataAccess = mock(DataAccess.class);
    dbCreateEntity = mock(DbCreateEntity.class);
    when(dataAccess.createEntity(any(),any(), any(), any(), any(), any())).thenReturn(dbCreateEntity);
    transactionState = mock(TransactionState.class);
    when(transactionState.wasCommitted()).thenReturn(false);
    when(dataAccess.executeAndReturn(dbCreateEntity)).thenReturn(transactionState);

    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION_NAME);
    entity = new CreateEntity(Lists.newArrayList());
    userId = "userId";
    baseCollection = Optional.empty();
    handleAdder = mock(HandleAdder.class);
  }

  @Test(expected = AuthorizationException.class)
  public void createEntityThrowsAnAuthorizationExceptionWhenTheUserIsNotAllowedToWriteToTheCollection()
    throws Exception {
    TimbuctooDbAccess instance = new TimbuctooDbAccess(notAllowedToWrite(), dataAccess, clock, handleAdder);

    instance.createEntity(mock(Collection.class), baseCollection, new CreateEntity(Lists.newArrayList()), "userId");
  }


  @Test
  public void createEntityLetsDataAccessSaveTheEntity() throws Exception {
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    UUID id = instance.createEntity(collection, baseCollection, this.entity, userId);

    InOrder inOrder = inOrder(dataAccess);
    inOrder.verify(dataAccess).createEntity(collection, baseCollection, this.entity, userId, instant, id);
    inOrder.verify(dataAccess).executeAndReturn(dbCreateEntity);
  }

  @Test
  public void createEntityReturnsTheId() throws Exception {
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    UUID id = instance.createEntity(collection, baseCollection, this.entity, userId);

    assertThat(id, is(notNullValue()));
  }

  @Ignore
  @Test
  public void createEntityLetsDataAccessSaveAnAdminVersionOfTheEntity() throws Exception {
    fail("Yet to be implemented");
  }

  @Test
  public void createEntityNotifiesHandleAdderThatANewEntityIsCreated() throws Exception {
    when(transactionState.wasCommitted()).thenReturn(true);
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    UUID id = instance.createEntity(collection, baseCollection, this.entity, userId);

    verify(handleAdder).add(new HandleAdderParameters(COLLECTION_NAME, id, 1));
  }

  @Test
  public void createEntityDoesNotCallTheHandleAdderWhenTheTransactionIsRolledBack() throws Exception {
    when(transactionState.wasCommitted()).thenReturn(false);
    TimbuctooDbAccess instance = new TimbuctooDbAccess(allowedToWrite(), dataAccess, clock, handleAdder);

    UUID id = instance.createEntity(collection, baseCollection, this.entity, userId);

    verify(handleAdder, never()).add(new HandleAdderParameters(COLLECTION_NAME, id, 1));
  }


  private Authorizer notAllowedToWrite() throws AuthorizationUnavailableException {
    return createAuthorizer(false);
  }

  private Authorizer allowedToWrite() throws AuthorizationUnavailableException {
    return createAuthorizer(true);
  }

  private Authorizer createAuthorizer(boolean allowedToWrite) throws AuthorizationUnavailableException {
    Authorizer authorizer = mock(Authorizer.class);
    Authorization authorization = mock(Authorization.class);
    when(authorization.isAllowedToWrite()).thenReturn(allowedToWrite);
    when(authorizer.authorizationFor(any(Collection.class), anyString())).thenReturn(authorization);
    return authorizer;
  }

}
