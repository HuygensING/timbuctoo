package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SystemRelationshipType.VERSION_OF;
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
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class Neo4JLowLevelAPITest {
  private static final String RELATIONSHIP_PROPERTY_WITHOUT_INDEX = "nonIndexedProperty";
  private static final String RELATION_PROPERTY_WITH_INDEX = "property";
  private static final String PROPERTY_VALUE = "test";
  private static final String DOMAIN_ENTITY_PROPERTY = SubADomainEntity.VALUEA2_NAME;
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final String ID = "id";
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;

  private Neo4JLowLevelAPI instance;
  private GraphDatabaseService dbMock;
  private GlobalGraphOperations globalGraphOperationsMock;
  private Transaction transactionMock;
  private RelationshipIndexes relationshipIndexesMock;

  @Before
  public void setup() {
    globalGraphOperationsMock = mock(GlobalGraphOperations.class);
    relationshipIndexesMock = mock(RelationshipIndexes.class);
    setupDBMock();
    instance = new Neo4JLowLevelAPI(dbMock, relationshipIndexesMock, globalGraphOperationsMock);
  }

  private void setupDBMock() {
    dbMock = mock(GraphDatabaseService.class);
    transactionMock = mock(Transaction.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  @Test
  public void addRelationshipIndexesTheRelationship() {
    // setup
    String startNodeId = "startNodeId";
    String endNodeId = "endNodeId";
    Relationship relationship = aRelationship() //
        .withProperty(ID_PROPERTY_NAME, ID)//
        .withStartNode(aNode().withId(startNodeId).build()) //
        .withEndNode(aNode().withId(endNodeId).build()) //
        .build();

    // action
    instance.addRelationship(relationship, ID);

    // verify
    verify(relationshipIndexesMock).indexByField(relationship, ID_PROPERTY_NAME, ID);
    verify(relationshipIndexesMock).indexByField(relationship, Relation.SOURCE_ID, startNodeId);
    verify(relationshipIndexesMock).indexByField(relationship, Relation.TARGET_ID, endNodeId);
  }

  @Test
  public void getLatestNodeByIdReturnsTheNodeWithTheHighestRevisionThatHasNoIncommingVersionOfRelations() {
    // setup
    NodeMockBuilder nodeBuildWithThirdRevision = aNode().withRevision(THIRD_REVISION);
    Node nodeWithThirdRevision = nodeBuildWithThirdRevision.build();
    Relationship versionOfRelationship = aRelationship().withType(VERSION_OF).build();
    Node nodeWithThirdRevisionAndIncommingVersionOfRelationNode = nodeBuildWithThirdRevision.withIncomingRelationShip(versionOfRelationship).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(FIRST_REVISION).build()) //
        .andNode(nodeWithThirdRevisionAndIncommingVersionOfRelationNode) //
        .andNode(nodeWithThirdRevision) //
        .andNode(aNode().withRevision(SECOND_REVISION).build()) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getLatestNodeById(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualNode, is(sameInstance(nodeWithThirdRevision)));

    transactionSucceeded();
  }

  private void transactionSucceeded() {
    verify(transactionMock).success();
  }

  @Test
  public void getLatestNodeByIdReturnsNullIfNoNodesAreFound() {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .foundInDB(dbMock);

    // action
    Node actualNode = instance.getLatestNodeById(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualNode, is(nullValue()));
    transactionSucceeded();
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
    transactionSucceeded();
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
    transactionSucceeded();
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
    transactionSucceeded();
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
  public void findNodeByPropertyReturnsTheFirstFoundNodeWithThePropertyThatHasNoIncomingVersionOfRelations() {
    // setup
    Relationship versionOf = aRelationship().withType(VERSION_OF).build();
    Node aNodeWithIncomingVersionOfRelations = aNode().withIncomingRelationShip(versionOf).build();
    Node aNodeWithoutIncommingRelations = aNode().build();
    Node otherNodeWithoutIncommingRelations = aNode().build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andPropertyWithValue(DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE) //
        .withNode(aNodeWithIncomingVersionOfRelations) //
        .andNode(aNodeWithoutIncommingRelations) //
        .andNode(otherNodeWithoutIncommingRelations) //
        .foundInDB(dbMock);

    // action
    Node foundNode = instance.findNodeByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE);

    // verify
    assertThat(foundNode, is(sameInstance(aNodeWithoutIncommingRelations)));

    verify(transactionMock).success();
  }

  @Test
  public void findNodeByPropertyReturnsNullIfNoNodesAreFound() {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL) //
        .andPropertyWithValue(DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE) //
        .foundInDB(dbMock);

    // action
    Node foundNode = instance.findNodeByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE);

    // verify
    assertThat(foundNode, is(nullValue()));

    verify(transactionMock).success();
  }

  @Test
  public void findNodeByPropertyReturnsNullIfNoNodeWithoutIncomingVersionOfRelationsAreFound() {
    // setup
    Relationship versionOf = aRelationship().withType(VERSION_OF).build();
    Node aNodeWithIncomingVersionOfRelations = aNode().withIncomingRelationShip(versionOf).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL) //
        .andPropertyWithValue(DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE) //
        .withNode(aNodeWithIncomingVersionOfRelations) //
        .foundInDB(dbMock);

    // action
    Node foundNode = instance.findNodeByProperty(DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE);

    // verify
    assertThat(foundNode, is(nullValue()));

    verify(transactionMock).success();
  }

  @Test
  public void getLatestRelationshipDelegatesToRelationshipIndexes() {
    // setup
    Relationship relationship = aRelationship().withRevision(THIRD_REVISION).build();
    when(relationshipIndexesMock.getLatestRelationshipById(ID)).thenReturn(relationship);

    // action
    Relationship actualRelationship = instance.getLatestRelationshipById(ID);

    // verify
    assertThat(actualRelationship, is(sameInstance(relationship)));
  }

  @Test
  public void getLatestRelationshipByIdReturnsNullWhenNoRelationshipsAreFoundForId() {
    // setup
    when(relationshipIndexesMock.getLatestRelationshipById(ID)).thenReturn(null);

    // action
    Relationship actualRelationship = instance.getLatestRelationshipById(ID);

    // verify
    assertThat(actualRelationship, is(nullValue()));
    verify(relationshipIndexesMock).getLatestRelationshipById(ID);
  }

  @Test
  public void getRelationshipDelegatesToRelationshipIndexes() {
    // setup
    Relationship relationship = aRelationship().withRevision(THIRD_REVISION).build();
    when(relationshipIndexesMock.getRelationshipWithRevision(ID, THIRD_REVISION)).thenReturn(relationship);

    // action
    Relationship actualRelationship = instance.getRelationshipWithRevision(RELATION_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualRelationship, is(sameInstance(relationship)));
  }

  @Test
  public void getRelationshipWithRevisionReturnsNullIfTheRevisionIsNoFound() {
    // setup
    when(relationshipIndexesMock.getRelationshipWithRevision(ID, THIRD_REVISION)).thenReturn(null);

    // action
    Relationship actualRelationship = instance.getRelationshipWithRevision(RELATION_TYPE, ID, THIRD_REVISION);

    // verify
    assertThat(actualRelationship, is(nullValue()));
  }

  @Test
  public void findRelationshipByPropertyReturnsTheFirstRelationshipThatIsTheLatestVersion() {
    // setup
    Relationship latestVersionOfRelationship = aRelationship().build();
    Relationship notLatestVersionOfRelationship2 = aRelationship().build();
    List<Relationship> foundRelationships = Lists.newArrayList(latestVersionOfRelationship, notLatestVersionOfRelationship2);
    foundRelationshipsFor(RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE, foundRelationships);

    isLatestVersion(latestVersionOfRelationship);
    isNotLatestVersion(notLatestVersionOfRelationship2);

    // action
    Relationship foundRelationship = instance.findRelationshipByProperty(RELATION_TYPE, RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // verify
    assertThat(foundRelationship, is(sameInstance(latestVersionOfRelationship)));
  }

  private void isLatestVersion(Relationship relationship) {
    when(relationshipIndexesMock.isLatestVersion(relationship)).thenReturn(true);
  }

  @Test
  public void findRelationshipByPropertyReturnsNullIfNoLatestRelationsAreFound() {
    // setup
    Relationship notLatestVersionOfRelationship1 = aRelationship().build();
    Relationship notLatestVersionOfRelationship2 = aRelationship().build();
    List<Relationship> foundRelationships = Lists.newArrayList(notLatestVersionOfRelationship1, notLatestVersionOfRelationship2);
    foundRelationshipsFor(RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE, foundRelationships);

    isNotLatestVersion(notLatestVersionOfRelationship1);
    isNotLatestVersion(notLatestVersionOfRelationship2);

    // action
    Relationship foundRelationship = instance.findRelationshipByProperty(RELATION_TYPE, RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // verify
    assertThat(foundRelationship, is(nullValue()));
  }

  private void isNotLatestVersion(Relationship relationship) {
    when(relationshipIndexesMock.isLatestVersion(relationship)).thenReturn(false);
  }

  private void foundRelationshipsFor(String propertyName, String propertyValue, List<Relationship> foundRelationships) {
    indexFor(propertyName);
    when(relationshipIndexesMock.getRelationshipsBy(propertyName, propertyValue)).thenReturn(foundRelationships);
  }

  @Test
  public void findRelationshipByPropertyReturnsNullIfNoRelationsAreFound() {
    // setup
    noRelationshipsFoundByProperty(RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // action
    Relationship foundRelationship = instance.findRelationshipByProperty(RELATION_TYPE, RELATION_PROPERTY_WITH_INDEX, PROPERTY_VALUE);

    // verify
    assertThat(foundRelationship, is(nullValue()));
  }

  private void noRelationshipsFoundByProperty(String propertyName, String propertyValue) {
    indexFor(propertyName);
    List<Relationship> foundRelationships = Lists.newArrayList();
    when(relationshipIndexesMock.getRelationshipsBy(propertyName, propertyValue)).thenReturn(foundRelationships);
  }

  private void indexFor(String propertyName) {
    when(relationshipIndexesMock.containsIndexFor(propertyName)).thenReturn(true);
  }

  @Test(expected = PropertyNotIndexedException.class)
  public void findRelationshipByPropertyThrowsAPropertyNotIndexedExceptionInThereIsNoIndexForTheField() {
    // setup
    noIndexFor(RELATIONSHIP_PROPERTY_WITHOUT_INDEX);

    // action
    instance.findRelationshipByProperty(RELATION_TYPE, RELATIONSHIP_PROPERTY_WITHOUT_INDEX, PROPERTY_VALUE);

    // verify
    transactionFailed();
  }

  private void noIndexFor(String propertyName) {
    when(relationshipIndexesMock.containsIndexFor(propertyName)).thenReturn(false);
  }

  private void transactionFailed() {
    verify(transactionMock).failure();
  }

  /*
   * The nodes with an incoming versionOf relationship will not be counted,
   *  because they are not the latest version.
   */
  @Test
  public void countNodesWithLabelReturnsTheNumberOfUniqueNodesWithACertainLabel() {
    // setup
    long two = 2l;
    Relationship versionOfRelationship = aRelationship().withType(VERSION_OF).build();
    NodeMockBuilder nodeWithVersionOfRelationship = aNode().withIncomingRelationShip(versionOfRelationship);
    String otherId = "otherId";
    ResourceIterable<Node> searchResultWithTwoNodes = aSearchResult()//
        .withNode(aNode().withId(ID).build())//
        .andNode(aNode().withId(otherId).build())//
        .andNode(nodeWithVersionOfRelationship.withId(ID).build()) //
        .andNode(nodeWithVersionOfRelationship.withId(otherId).build()) //
        .build();

    when(globalGraphOperationsMock.getAllNodesWithLabel(DOMAIN_ENTITY_LABEL)).thenReturn(searchResultWithTwoNodes);

    // action
    long count = instance.countNodesWithLabel(DOMAIN_ENTITY_LABEL);

    // verify
    assertThat(count, is(equalTo(two)));
    transactionSucceeded();
  }

  @Test
  public void countRelationshipsDelegatesToRelationshipIndexes() {
    // setup
    long count = 5l;
    when(relationshipIndexesMock.countRelationships()).thenReturn(count);

    // action
    long actualCount = instance.countRelationships();

    // verify
    assertThat(actualCount, is(equalTo(count)));
  }
}
