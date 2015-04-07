package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;

public class Neo4JStorageIteratorFactoryTest {
  private static final Class<Relation> RELATION_TYPE = Relation.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private Neo4JStorageIteratorFactory instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;

  @Before
  public void setUp() {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
    instance = new Neo4JStorageIteratorFactory(propertyContainerConverterFactoryMock);
  }

  @Test
  public void forNodeReturnsAStorageIteratorForEntities() throws Exception {
    // setup
    @SuppressWarnings("unchecked")
    NodeConverter<TestSystemEntityWrapper> nodeConverter = mock(NodeConverter.class);
    Node node1 = aNode().build();
    Node node2 = aNode().build();

    TestSystemEntityWrapper entity1 = aSystemEntity().build();
    TestSystemEntityWrapper entity2 = aSystemEntity().build();
    when(nodeConverter.convertToEntity(node1)).thenReturn(entity1);
    when(nodeConverter.convertToEntity(node2)).thenReturn(entity2);

    when(propertyContainerConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(nodeConverter);

    List<Node> nodes = Lists.newArrayList(node1, node2);

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.forNode(SYSTEM_ENTITY_TYPE, nodes.iterator());

    // verify
    assertThat(storageIterator.getAll(), containsInAnyOrder(entity1, entity2));
  }

  @Test(expected = ConversionException.class)
  public void forNodeThrowsAConversionExceptionWhenOneOfTheNodesCannotBeConverted() throws Exception {
    forNodeThrowsAnException(new ConversionException());
  }

  @Test(expected = StorageException.class)
  public void forNodeThrowsAStorageExceptionWhenTheEntityCannotBeInstantiated() throws Exception {
    forNodeThrowsAnException(new InstantiationException());
  }

  private void forNodeThrowsAnException(Exception exceptionToThrow) throws ConversionException, InstantiationException, StorageException {
    // setup
    @SuppressWarnings("unchecked")
    NodeConverter<TestSystemEntityWrapper> nodeConverter = mock(NodeConverter.class);
    Node node = aNode().build();
    when(nodeConverter.convertToEntity(node)).thenThrow(exceptionToThrow);

    when(propertyContainerConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(nodeConverter);

    List<Node> nodes = Lists.newArrayList(node);

    // action
    instance.forNode(SYSTEM_ENTITY_TYPE, nodes.iterator());
  }

  @Test
  public void forRelationshipReturnsAStorageIteratorForRelations() throws Exception {
    // setup
    @SuppressWarnings("unchecked")
    RelationshipConverter<Relation> relationshipConverter = mock(RelationshipConverter.class);
    Relationship relationship1 = aRelationship().build();
    Relationship relationship2 = aRelationship().build();
    Relation relation1 = new Relation();
    Relation relation2 = new Relation();
    when(relationshipConverter.convertToEntity(relationship1)).thenReturn(relation1);
    when(relationshipConverter.convertToEntity(relationship2)).thenReturn(relation2);

    when(propertyContainerConverterFactoryMock.createForRelation(RELATION_TYPE)).thenReturn(relationshipConverter);

    List<Relationship> relationships = Lists.newArrayList(relationship1, relationship2);

    // action
    StorageIterator<Relation> storageIterator = instance.forRelationship(RELATION_TYPE, relationships);

    // verify
    assertThat(storageIterator.getAll(), containsInAnyOrder(relation1, relation2));
  }

  @Test(expected = ConversionException.class)
  public void forRelationshipThrowsAConversionExceptionWhenOneOfTheRelationshipsCannotBeConverted() throws Exception {
    forRelationshipThrowsAnException(new ConversionException());
  }

  private void forRelationshipThrowsAnException(Exception exceptionToThrow) throws ConversionException, InstantiationException, StorageException {
    // setup
    @SuppressWarnings("unchecked")
    RelationshipConverter<Relation> relationshipConverter = mock(RelationshipConverter.class);
    Relationship relationship1 = aRelationship().build();
    when(relationshipConverter.convertToEntity(relationship1)).thenThrow(exceptionToThrow);

    when(propertyContainerConverterFactoryMock.createForRelation(RELATION_TYPE)).thenReturn(relationshipConverter);

    List<Relationship> relationships = Lists.newArrayList(relationship1);

    // action
    instance.forRelationship(RELATION_TYPE, relationships);
  }

  @Test(expected = StorageException.class)
  public void forRelationshipThrowsAStorageExceptionWhenOneOfTheRelationsCannotBeInstantiated() throws Exception {
    forRelationshipThrowsAnException(new InstantiationException());
  }

}
