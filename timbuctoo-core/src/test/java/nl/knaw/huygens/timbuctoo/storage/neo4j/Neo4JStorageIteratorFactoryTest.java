package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeSearchResultBuilder.aNodeSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JStorageIteratorFactory.Neo4JStorageIterator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;

import test.model.TestSystemEntityWrapper;

public class Neo4JStorageIteratorFactoryTest {
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;

  @Test
  public void createCreatesANeo4JStorageIterator() {
    PropertyContainerConverterFactory pccf = mock(PropertyContainerConverterFactory.class);
    @SuppressWarnings("unchecked")
    NodeConverter<TestSystemEntityWrapper> nodeConverterMock = mock(NodeConverter.class);
    when(pccf.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(nodeConverterMock);

    Neo4JStorageIteratorFactory instance = new Neo4JStorageIteratorFactory(pccf);
    ResourceIterable<Node> searchResult = aNodeSearchResult().build();

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.create(SYSTEM_ENTITY_TYPE, searchResult);

    // verify
    assertThat(storageIterator, is(instanceOf(Neo4JStorageIterator.class)));

    Neo4JStorageIterator<TestSystemEntityWrapper> neo4jStorageIterator = (Neo4JStorageIterator<TestSystemEntityWrapper>) storageIterator;

    assertThat(neo4jStorageIterator.converter, is(sameInstance(nodeConverterMock)));
    assertThat(neo4jStorageIterator.delegate, is(sameInstance(searchResult.iterator())));
  }
}
