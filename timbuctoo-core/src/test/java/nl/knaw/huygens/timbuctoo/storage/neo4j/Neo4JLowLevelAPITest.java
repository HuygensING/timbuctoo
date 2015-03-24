package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JLegacyStorageWrapper.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class Neo4JLowLevelAPITest {
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final String ID = "id";
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;

  private Neo4JLowLevelAPI instance;
  private GraphDatabaseService dbMock;
  private Transaction transactionMock;

  @Before
  public void setup() {
    setupDBMock();
    instance = new Neo4JLowLevelAPI(dbMock);
  }

  private void setupDBMock() {
    dbMock = mock(GraphDatabaseService.class);
    transactionMock = mock(Transaction.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  @Test
  public void getRevisionPropertyReturnsTheValueOfTheRevisionPropertyAsInt() {
    // setup
    int revision = 1;
    Node node = aNode().withRevision(revision).build();

    // action
    int actualRevision = instance.getRevisionProperty(node);

    // verify
    assertThat(actualRevision, is(equalTo(revision)));
  }

  @Test
  public void getRevisionPropertyReturnsZeroIfThePropertyContainerHasNoPropertyRevision() {
    // setup
    Node nodeWithoutRevision = aNode().build();

    // action
    int actualRevision = instance.getRevisionProperty(nodeWithoutRevision);

    // verify
    assertThat(actualRevision, is(equalTo(0)));
  }

  @Test
  public void getRevisionPropertyReturnsZeroIfThePropertyContainerIsNull() {
    // setup
    Node nullNode = null;

    // action
    int actualRevision = instance.getRevisionProperty(nullNode);

    // verify
    assertThat(actualRevision, is(equalTo(0)));
  }

  @Test
  public void getLatestByIdReturnsTheNodeWithTheHighestRevision() {
    // setup
    Node nodeWithThirdRevision = aNode().withRevision(THIRD_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(FIRST_REVISION).build()) //
        .andNode(nodeWithThirdRevision) //
        .andNode(aNode().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getLatestNodeById(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualNode, is(sameInstance(nodeWithThirdRevision)));

    transactionSuccess();
  }

  private void transactionSuccess() {
    verify(transactionMock).success();
  }

  @Test
  public void getLatestByIdReturnsNullIfNoNodesAreFound() {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getLatestNodeById(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualNode, is(nullValue()));
    transactionSuccess();
  }

  @Test
  public void getNodeWithRevisionReturnsTheNodeForIdAndRevision() {
    // setup
    Node nodeWithThirdRevision = aNode().withRevision(THIRD_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(FIRST_REVISION).build()) //
        .andNode(nodeWithThirdRevision) //
        .andNode(aNode().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getNodeWithRevision(DOMAIN_ENTITY_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualNode, is(sameInstance(nodeWithThirdRevision)));
    transactionSuccess();
  }

  @Test
  public void getNodeWithRevisionReturnsNullIfNoNodesAreFound() {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getNodeWithRevision(DOMAIN_ENTITY_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualNode, is(nullValue()));
    transactionSuccess();
  }

  @Test
  public void getNodeWithRevisionReturnsNullIfTheRevisionIsNotFound() {
    // setup
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getNodeWithRevision(DOMAIN_ENTITY_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualNode, is(nullValue()));
    transactionSuccess();
  }

  @Test
  public void getAllNodesWithTimbuctooIdReturnsAListWithTheFoundNodes() {
    // setup
    Node node1 = aNode().withRevision(FIRST_REVISION).build();
    Node node2 = aNode().withRevision(SECOND_REVISION).build();
    Node node3 = aNode().withRevision(THIRD_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(node1) //
        .andNode(node3) //
        .andNode(node2) //
        .foundInDB(dbMock);

    // action
    List<Node> foundNodes = instance.getNodesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundNodes, containsInAnyOrder(node1, node2, node3));
  }

  @Test
  public void getAllNodesWithTimbuctooIdReturnsAnEmptyListWhenNoNodesAreFound() {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .foundInDB(dbMock);

    // action
    List<Node> foundNodes = instance.getNodesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundNodes, is(empty()));

  }

  @Test
  public void getLatestRelationshipReturnsTheRelationshipWithTheHighestRevisionWithId() {
    // setup
    Relationship relationshipThirdRevision = aRelationship().withRevision(THIRD_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(aRelationship().withRevision(FIRST_REVISION).build()) //
        .andRelationship(relationshipThirdRevision) //
        .andRelationship(aRelationship().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Relationship actualRelationship = instance.getLatestRelationship(ID);

    // verify
    assertThat(actualRelationship, is(sameInstance(relationshipThirdRevision)));
    transactionSuccess();
  }

  @Test
  public void getLatestRelationshipReturnsNullWhenNoRelationshipsAreFoundForId() {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID) //
        .foundInDB(dbMock);

    // action
    Relationship actualRelationship = instance.getLatestRelationship(ID);

    // verify
    assertThat(actualRelationship, is(nullValue()));
    transactionSuccess();
  }

  @Test
  public void getRelationshipWithRevisionReturnsTheRelationshipForTheIdAndRevision() {
    // setup
    Relationship relationshipThirdRevision = aRelationship().withRevision(THIRD_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(aRelationship().withRevision(FIRST_REVISION).build()) //
        .andRelationship(relationshipThirdRevision) //
        .andRelationship(aRelationship().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Relationship actualRelationship = instance.getRelationshipWithRevision(RELATION_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualRelationship, is(sameInstance(relationshipThirdRevision)));
    transactionSuccess();
  }

  @Test
  public void getRelationshipWithRevisionReturnsNullIfNoRelationsAreFound() {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID) //
        .foundInDB(dbMock);

    // action
    Relationship actualRelationship = instance.getRelationshipWithRevision(RELATION_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualRelationship, is(nullValue()));
    transactionSuccess();
  }

  @Test
  public void getRelationshipWithRevisionReturnsNullIfTheRevisionIsNotFound() {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(aRelationship().withRevision(FIRST_REVISION).build()) //
        .andRelationship(aRelationship().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Relationship actualRelationship = instance.getRelationshipWithRevision(RELATION_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualRelationship, is(nullValue()));
    transactionSuccess();
  }

}
