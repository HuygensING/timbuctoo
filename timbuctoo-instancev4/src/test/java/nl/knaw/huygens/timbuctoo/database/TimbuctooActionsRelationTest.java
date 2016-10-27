package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.CreateMessage.failure;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimbuctooActionsRelationTest {

  private static final String USER_ID = "userId";
  private TransactionEnforcer transactionEnforcer;
  private Clock clock;
  private HandleAdder handleAdder;
  private CreateRelation createRelation;
  private Collection collection;
  private Instant instant;

  @Before
  public void setUp() throws Exception {
    transactionEnforcer = mock(TransactionEnforcer.class);
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    handleAdder = mock(HandleAdder.class);
    createRelation = new CreateRelation(null, null, null);
    collection = mock(Collection.class);
  }

  @Test
  public void createRelationCreatesANewRelation() throws Exception {
    when(transactionEnforcer.createRelation(collection, createRelation))
      .thenReturn(CreateMessage.success(UUID.randomUUID()));
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.createRelation(collection, createRelation, USER_ID);

    verify(transactionEnforcer).createRelation(argThat(is(collection)), argThat(allOf(
      hasProperty("created", allOf(
        hasProperty("userId", is(USER_ID)),
        hasProperty("timeStamp", is(instant.toEpochMilli()))
      ))
    )));

  }

  @Test
  public void createRelationReturnsTheIdOfTheNewLyCreatedRelation() throws Exception {
    when(transactionEnforcer.createRelation(collection, createRelation))
      .thenReturn(CreateMessage.success(UUID.randomUUID()));
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    UUID id = instance.createRelation(collection, createRelation, USER_ID);

    assertThat(id, is(notNullValue(UUID.class)));
  }

  @Test(expected = AuthorizationException.class)
  public void createRelationThrowsAnUnauthorizedExceptionWhenTheUserIsNotAllowedToWrite() throws Exception {
    TimbuctooActions instance = new TimbuctooActions(notAllowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    try {
      instance.createRelation(collection, createRelation, USER_ID);
    } finally {
      verifyZeroInteractions(transactionEnforcer);
    }
  }

  @Test(expected = IOException.class)
  public void createRelationsThrowsAnIoExceptionWithTheMessageOfTheReturnValueIfTheRelationsCouldNotBeCreated()
    throws Exception {
    when(transactionEnforcer.createRelation(collection, createRelation)).thenReturn(failure("error message"));
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.createRelation(collection, createRelation, USER_ID);
  }

  @Test(expected = AuthorizationException.class)
  public void replaceRelationThrowsAnAuthorizationExceptionWhenTheUsersIsNotAllowedToWrite() throws Exception {
    TimbuctooActions instance = new TimbuctooActions(notAllowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    try {
      instance.replaceRelation(collection, new UpdateRelation(UUID.randomUUID(), 1, false), USER_ID);
    } finally {
      verifyZeroInteractions(transactionEnforcer);
    }
  }

  @Test
  public void replaceRelationUpdatesARelation() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateRelation updateRelation = new UpdateRelation(id, 1, false);
    when(transactionEnforcer.updateRelation(collection, updateRelation)).thenReturn(UpdateReturnMessage.success(1));
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.replaceRelation(collection, updateRelation, USER_ID);

    verify(transactionEnforcer).updateRelation(argThat(is(collection)), argThat(allOf(
      hasProperty("id", is(id)),
      hasProperty("modified", allOf(
        hasProperty("userId", is(USER_ID)),
        hasProperty("timeStamp", is(instant.toEpochMilli()))
      ))
    )));
  }

  @Test(expected = NotFoundException.class)
  public void replaceRelationThrowsANotFoundExceptionWhenTheRelationCannotBeFound() throws Exception {
    UpdateRelation updateRelation = new UpdateRelation(null, 1, false);
    when(transactionEnforcer.updateRelation(collection, updateRelation)).thenReturn(UpdateReturnMessage.notFound());
    TimbuctooActions instance = new TimbuctooActions(allowedToWrite(), transactionEnforcer, clock, handleAdder,
      mock(DataStoreOperations.class));

    instance.replaceRelation(collection, updateRelation, USER_ID);
  }

}
