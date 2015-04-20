package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.Neo4JLowLevelAPI.GET_ALL_QUERY;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.Neo4JLowLevelAPI.LABEL_PROPERTY;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeSearchResultBuilder.aNodeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeSearchResultBuilder.anEmptyNodeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipMockBuilder.aRelationship;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
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
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class Neo4JLowLevelAPITest {
  private static final String RELATION_TYPE_ID = "relationTypeId";
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
  private Transaction transactionMock;
  private RelationshipIndexes relationshipIndexesMock;
  private Index<Node> nodeIndexMock;

  @Before
  public void setup() {
    relationshipIndexesMock = mock(RelationshipIndexes.class);
    setupDBMock();
    instance = new Neo4JLowLevelAPI(dbMock, relationshipIndexesMock);
  }

  private void setupDBMock() {
    dbMock = mock(GraphDatabaseService.class);
    transactionMock = mock(Transaction.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
    setupIndexManager();
  }

  @SuppressWarnings("unchecked")
  private void setupIndexManager() {
    nodeIndexMock = mock(Index.class);
    IndexManager indexManager = mock(IndexManager.class);
    when(dbMock.index()).thenReturn(indexManager);
    when(indexManager.forNodes(LABEL_PROPERTY)).thenReturn(nodeIndexMock);
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

    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withPropertyContainer(aNode().withIncomingRelationShip(versionOfRelationship).withRevision(FIRST_REVISION).build()) //
        .andPropertyContainer(nodeWithThirdRevisionAndIncommingVersionOfRelationNode) //
        .andPropertyContainer(nodeWithThirdRevision) //
        .andPropertyContainer(aNode().withIncomingRelationShip(versionOfRelationship).withRevision(SECOND_REVISION).build()) //
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
    anEmptyNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
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

    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withPropertyContainer(aNode().withRevision(FIRST_REVISION).build()) //
        .andPropertyContainer(nodeWithThirdRevision) //
        .andPropertyContainer(aNode().withRevision(SECOND_REVISION).build()) //
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
    anEmptyNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
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
    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withPropertyContainer(aNode().withRevision(SECOND_REVISION).build()) //
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

    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withPropertyContainer(node1) //
        .andPropertyContainer(node3) //
        .andPropertyContainer(node2) //
        .foundInDB(dbMock);

    // action
    List<Node> foundNodes = instance.getNodesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundNodes, containsInAnyOrder(node1, node2, node3));
  }

  @Test
  public void getAllNodesWithTimbuctooIdReturnsAnEmptyListWhenNoNodesAreFound() {
    // setup
    anEmptyNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .foundInDB(dbMock);

    // action
    List<Node> foundNodes = instance.getNodesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(foundNodes, is(empty()));

  }

  @Test
  public void getNodesOfTypeReturnsAResourceIteratorWithOfTheFoundNodes() {
    // setup
    Node latestNode1 = aNode().build();
    Node latestNode2 = aNode().build();
    ResourceIterator<Node> searchResult = aNodeSearchResult() //
        .withPropertyContainer(latestNode1) //
        .andPropertyContainer(latestNode2) //
        .asIterator();

    @SuppressWarnings("unchecked")
    IndexHits<Node> foudNodes = mock(IndexHits.class);
    when(foudNodes.iterator()).thenReturn(searchResult);
    when(nodeIndexMock.get(LABEL_PROPERTY, DOMAIN_ENTITY_LABEL.name())).thenReturn(foudNodes);

    // action
    ResourceIterator<Node> actualSearchResult = instance.getNodesOfType(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(actualSearchResult, is(sameInstance(searchResult)));
    assertThat(Lists.newArrayList(actualSearchResult), containsInAnyOrder(latestNode1, latestNode2));

    verify(transactionMock).success();
  }

  @Test
  public void findNodeByPropertyReturnsTheFirstFoundNodeWithThePropertyThatHasNoIncomingVersionOfRelations() {
    // setup
    Relationship versionOf = aRelationship().withType(VERSION_OF).build();
    Node aNodeWithIncomingVersionOfRelations = aNode().withIncomingRelationShip(versionOf).build();
    Node aNodeWithoutIncommingRelations = aNode().build();
    Node otherNodeWithoutIncommingRelations = aNode().build();

    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andPropertyWithValue(DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE) //
        .withPropertyContainer(aNodeWithIncomingVersionOfRelations) //
        .andPropertyContainer(aNodeWithoutIncommingRelations) //
        .andPropertyContainer(otherNodeWithoutIncommingRelations) //
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
    anEmptyNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL) //
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

    aNodeSearchResult().forLabel(DOMAIN_ENTITY_LABEL) //
        .andPropertyWithValue(DOMAIN_ENTITY_PROPERTY, PROPERTY_VALUE) //
        .withPropertyContainer(aNodeWithIncomingVersionOfRelations) //
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
  public void getRelationshipsByNodeIdReturnsTheIncomingAndOutgoingRelationshipsOfTheNode() {
    // setup
    List<Relationship> outgoing = relationshipList(2);
    when(relationshipIndexesMock.getRelationshipsBy(SOURCE_ID, ID)).thenReturn(outgoing);

    List<Relationship> incoming = relationshipList(3);
    when(relationshipIndexesMock.getRelationshipsBy(TARGET_ID, ID)).thenReturn(incoming);

    // action
    List<Relationship> foundRelationships = instance.getRelationshipsByNodeId(ID);

    // verify
    assertThat(foundRelationships, hasSize(5));
    assertThat(foundRelationships, hasItems(outgoing.toArray(new Relationship[0])));
    assertThat(foundRelationships, hasItems(incoming.toArray(new Relationship[0])));

  }

  private List<Relationship> relationshipList(int size) {
    List<Relationship> relationships = Lists.newArrayList();

    for (int i = 0; i < size; i++) {
      relationships.add(aRelationship().build());
    }

    return relationships;
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
    ResourceIterator<Node> searchResultWithTwoNodes = aNodeSearchResult()//
        .withPropertyContainer(aNode().withId(ID).build())//
        .andPropertyContainer(aNode().withId(otherId).build())//
        .andPropertyContainer(nodeWithVersionOfRelationship.withId(ID).build()) //
        .andPropertyContainer(nodeWithVersionOfRelationship.withId(otherId).build()) //
        .asIterator();

    @SuppressWarnings("unchecked")
    IndexHits<Node> indexHits = mock(IndexHits.class);
    when(indexHits.iterator()).thenReturn(searchResultWithTwoNodes);
    when(nodeIndexMock.get(LABEL_PROPERTY, DOMAIN_ENTITY_LABEL.name())).thenReturn(indexHits);

    // action
    long count = instance.countNodesWithLabel(DOMAIN_ENTITY_LABEL);

    // verify
    assertThat(count, is(equalTo(two)));
    transactionSucceeded();
  }

  @Test
  public void countRelationshipsReturnsTheNumberOfUniqueRelationship() {
    // setup   
    long two = 2l;

    Node node1 = aNode().withIncomingRelationShip(aRelationship().withType(VERSION_OF).build()).build();

    RelationshipMockBuilder relationshipBuilderWithId = aRelationship().withProperty(ID_PROPERTY_NAME, ID);
    Relationship rel1 = relationshipBuilderWithId.withRevision(FIRST_REVISION).build();
    Relationship rel1V2 = relationshipBuilderWithId.withRevision(SECOND_REVISION).build();

    // latest nodes do not have incoming relationships of VERSION_OF type 
    Node node1V2 = aNode().withRevision(SECOND_REVISION)//
        .withOutgoingRelationShip(rel1) //
        .andOutgoingRelationship(rel1V2)//
        .build();

    String otherId = "otherId";
    RelationshipMockBuilder relationshipBuilderWithOtherId = aRelationship().withProperty(ID_PROPERTY_NAME, otherId);
    Relationship rel2 = relationshipBuilderWithOtherId.withRevision(FIRST_REVISION).build();
    Relationship rel2V2 = relationshipBuilderWithOtherId.withRevision(SECOND_REVISION).build();

    Node node2 = aNode().withOutgoingRelationShip(rel2).andOutgoingRelationship(rel2V2).build();

    ResourceIterator<Node> searchResult = aNodeSearchResult().withPropertyContainer(node1V2).andPropertyContainer(node1).andPropertyContainer(node2).asIterator();

    @SuppressWarnings("unchecked")
    IndexHits<Node> indexHits = mock(IndexHits.class);
    when(indexHits.iterator()).thenReturn(searchResult);
    when(nodeIndexMock.query(GET_ALL_QUERY)).thenReturn(indexHits);

    // action
    long actualCount = instance.countRelationships();

    // verify
    assertThat(actualCount, is(equalTo(two)));
    transactionSucceeded();
  }

  @Test
  public void findLatestRelationshipForDelegatesToRelationshipIndexes() {
    // setup
    Relationship relationship = aRelationship().build();
    when(relationshipIndexesMock.findLatestRelationshipFor(SOURCE_ID, TARGET_ID, RELATION_TYPE_ID)).thenReturn(relationship);

    // action
    Relationship foundRelationship = instance.findLatestRelationshipFor(RELATION_TYPE, SOURCE_ID, TARGET_ID, RELATION_TYPE_ID);

    // verify
    assertThat(foundRelationship, is(sameInstance(relationship)));
  }

  @Test
  public void indexIndexesTheNodeForAllItsLabels() {
    // setup
    Label otherLabel = DynamicLabel.label("otherLabel");
    Label anotherLabel = DynamicLabel.label("anotherLabel");
    Node nodeMock = aNode().withLabel(DOMAIN_ENTITY_LABEL)//
        .withLabel(otherLabel).withLabel(anotherLabel).build();

    // action
    instance.index(nodeMock);

    // verify
    verify(nodeIndexMock).add(nodeMock, LABEL_PROPERTY, DOMAIN_ENTITY_LABEL.name());
    verify(nodeIndexMock).add(nodeMock, LABEL_PROPERTY, otherLabel.name());
    verify(nodeIndexMock).add(nodeMock, LABEL_PROPERTY, anotherLabel.name());
  }
}
