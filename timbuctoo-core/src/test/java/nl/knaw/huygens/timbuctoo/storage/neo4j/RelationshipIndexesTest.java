package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.IndexManagerMockBuilder.anIndexManager;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndex;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.RelationshipIndex;

import com.google.common.collect.Lists;

public class RelationshipIndexesTest {

  private static final String PROPERTY_VALUE = "Test";
  private static final String PROPERTY_WITHOUT_INDEX = "test";
  private static final String PROPERTY_WITH_INDEX = "test2";
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final String ID = "id";
  private RelationshipIndexes instance;
  private GraphDatabaseService dbMock;
  private Transaction transactionMock;

  @Before
  public void setup() {
    setupDbMock();
    instance = new RelationshipIndexes(dbMock, Lists.newArrayList(PROPERTY_WITH_INDEX));
  }

  private void setupDbMock() {
    dbMock = mock(GraphDatabaseService.class);
    transactionMock = mock(Transaction.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  @Test
  public void containsIndexForReturnsFalseIfAPropertyIsNotIndexed() {
    // action
    boolean containsIndex = instance.containsIndexFor(PROPERTY_WITHOUT_INDEX);

    // verify
    assertThat(containsIndex, is(equalTo(false)));
  }

  @Test
  public void containsIndexForReturnsTryeIfAPropertyIsIndexed() {
    // action
    boolean containsIndex = instance.containsIndexFor(PROPERTY_WITH_INDEX);

    // verify
    assertThat(containsIndex, is(equalTo(true)));
  }

  @Test
  public void getRelationshipByReturnsAllTheRelationsfoundWithAPropertyValue() {
    // setup
    Relationship relationship1 = aRelationship().build();
    Relationship relationship2 = aRelationship().build();
    RelationshipIndex index = aRelationshipIndex() //
        .containsForPropertyWithValue(PROPERTY_WITH_INDEX, PROPERTY_VALUE) //
        .relationship(relationship1) //
        .andRelationship(relationship2) //
        .build();

    anIndexManager().containsRelationshipIndexWithName(index, PROPERTY_WITH_INDEX)//
        .foundInDB(dbMock);

    // action
    List<Relationship> foundRelationships = instance.getRelationshipsBy(PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // verify
    assertThat(foundRelationships, containsInAnyOrder(relationship1, relationship2));

    transactionSucceeded();
  }

  @Test
  public void getRelationshipByReturnsAnEmptyListWhenNoRelationsAreFound() {
    // setup
    RelationshipIndex index = aRelationshipIndex() //
        .containsNothingForPropertyWithValue(PROPERTY_WITH_INDEX, PROPERTY_VALUE) //
        .build();

    anIndexManager().containsRelationshipIndexWithName(index, PROPERTY_WITH_INDEX)//
        .foundInDB(dbMock);

    // action
    List<Relationship> foundRelationships = instance.getRelationshipsBy(PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // verify
    assertThat(foundRelationships, is(empty()));

    transactionSucceeded();
  }

  @Test
  public void isLatestVersionRequestsAllTheRelationshipsWithTheSameIdAndChecksIfTheVersionIsTheSameAsTheHighest() {
    // setup
    RelationshipMockBuilder aRelationshipWithId = aRelationship().withProperty(ID_PROPERTY_NAME, ID);
    RelationshipIndex index = aRelationshipIndex().containsForId(ID) //
        .relationship(aRelationshipWithId.withRevision(FIRST_REVISION).build()) //
        .andRelationship(aRelationshipWithId.withRevision(THIRD_REVISION).build()) //
        .andRelationship(aRelationshipWithId.withRevision(SECOND_REVISION).build()) //
        .build();

    anIndexManager().containsRelationshipIndexWithName(index, ID_PROPERTY_NAME) //
        .foundInDB(dbMock);

    Relationship relationToCheck = aRelationshipWithId//
        .withRevision(THIRD_REVISION)//
        .build();

    // action
    boolean isLatest = instance.isLatestVersion(relationToCheck);

    // verify
    assertThat(isLatest, is(equalTo(true)));

    transactionSucceeded();
  }

  @Test
  public void isLatestVersionReturnsFalseIfTheRelationshipDoesNotHaveTheHighestRevision() {
    // setup
    RelationshipMockBuilder relationshipWithId = aRelationship().withProperty(ID_PROPERTY_NAME, ID);
    RelationshipIndex index = aRelationshipIndex().containsForId(ID) //
        .relationship(relationshipWithId.withRevision(FIRST_REVISION).build()) //
        .andRelationship(relationshipWithId.withRevision(THIRD_REVISION).build()) //
        .andRelationship(relationshipWithId.withRevision(SECOND_REVISION).build()) //
        .build();

    anIndexManager().containsRelationshipIndexWithName(index, ID_PROPERTY_NAME) //
        .foundInDB(dbMock);

    Relationship relationToCheck = relationshipWithId//
        .withRevision(SECOND_REVISION)//
        .build();

    // action
    boolean isLatest = instance.isLatestVersion(relationToCheck);

    // verify
    assertThat(isLatest, is(equalTo(false)));

    transactionSucceeded();
  }

  private void transactionSucceeded() {
    verify(transactionMock).success();
  }
}
