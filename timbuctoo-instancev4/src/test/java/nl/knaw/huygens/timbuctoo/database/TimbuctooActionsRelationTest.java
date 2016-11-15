package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.allowedToWrite;
import static nl.knaw.huygens.timbuctoo.database.AuthorizerBuilder.notAllowedToWrite;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimbuctooActionsRelationTest {

  private static final String USER_ID = "userId";
  private Clock clock;
  private HandleAdder handleAdder;
  private CreateRelation createRelation;
  private Collection collection;
  private Instant instant;
  private DataStoreOperations dataStoreOperations;
  private AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  @Before
  public void setUp() throws Exception {
    clock = mock(Clock.class);
    instant = Instant.now();
    when(clock.instant()).thenReturn(instant);
    handleAdder = mock(HandleAdder.class);
    createRelation = new CreateRelation(null, null, null);
    collection = mock(Collection.class);
    dataStoreOperations = mock(DataStoreOperations.class);
    afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
  }

  @Test
  public void createRelationCreatesANewRelation() throws Exception {
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.createRelation(collection, createRelation, USER_ID);

    verify(dataStoreOperations).acceptRelation(argThat(is(collection)), argThat(allOf(
      hasProperty("created", allOf(
        hasProperty("userId", is(USER_ID)),
        hasProperty("timeStamp", is(instant.toEpochMilli()))
      ))
    )));

  }

  @Test
  public void createRelationReturnsTheIdOfTheNewLyCreatedRelation() throws Exception {
    when(dataStoreOperations.acceptRelation(collection, createRelation))
      .thenReturn(UUID.randomUUID());
    TimbuctooActions instance = createInstance(allowedToWrite());

    UUID id = instance.createRelation(collection, createRelation, USER_ID);

    assertThat(id, is(notNullValue(UUID.class)));
  }

  @Test(expected = AuthorizationException.class)
  public void createRelationThrowsAnUnauthorizedExceptionWhenTheUserIsNotAllowedToWrite() throws Exception {
    TimbuctooActions instance = createInstance(notAllowedToWrite());

    try {
      instance.createRelation(collection, createRelation, USER_ID);
    } finally {
      verifyZeroInteractions(dataStoreOperations);
    }
  }

  @Test(expected = IOException.class)
  public void createRelationsThrowsAnIoExceptionWithTheMessageOfTheReturnValueIfTheRelationsCouldNotBeCreated()
    throws Exception {
    when(dataStoreOperations.acceptRelation(collection, createRelation))
      .thenThrow(new RelationNotPossibleException(""));
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.createRelation(collection, createRelation, USER_ID);
  }

  @Test(expected = AuthorizationException.class)
  public void replaceRelationThrowsAnAuthorizationExceptionWhenTheUsersIsNotAllowedToWrite() throws Exception {
    TimbuctooActions instance = createInstance(notAllowedToWrite());

    try {
      instance.replaceRelation(collection, new UpdateRelation(UUID.randomUUID(), 1, false), USER_ID);
    } finally {
      verifyZeroInteractions(dataStoreOperations);
    }
  }

  @Test
  public void replaceRelationUpdatesARelation() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateRelation updateRelation = new UpdateRelation(id, 1, false);
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceRelation(collection, updateRelation, USER_ID);

    verify(dataStoreOperations).replaceRelation(argThat(is(collection)), argThat(allOf(
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
    doThrow(new NotFoundException()).when(dataStoreOperations).replaceRelation(collection, updateRelation);
    TimbuctooActions instance = createInstance(allowedToWrite());

    instance.replaceRelation(collection, updateRelation, USER_ID);
  }

  private TimbuctooActions createInstance(Authorizer authorizer) throws AuthorizationUnavailableException {
    return new TimbuctooActions(authorizer, clock, handleAdder,
      dataStoreOperations, afterSuccessTaskExecutor);
  }

}
