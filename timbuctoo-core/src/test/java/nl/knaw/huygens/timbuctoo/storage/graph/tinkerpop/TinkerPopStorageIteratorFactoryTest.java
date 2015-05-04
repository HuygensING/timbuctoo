package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopStorageIteratorFactoryTest {
  private static final Class<TestSystemEntityWrapper> ENTITY_TYPE = TestSystemEntityWrapper.class;
  @SuppressWarnings("rawtypes")
  private static final Class<TinkerPopIterator> TINKERPOP_ITERATOR_TYPE = TinkerPopIterator.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private ElementConverterFactory elementConverterFactory;
  private TinkerPopStorageIteratorFactory instance;

  @Before
  public void setup() {
    elementConverterFactory = mock(ElementConverterFactory.class);
    instance = new TinkerPopStorageIteratorFactory(elementConverterFactory);
  }

  @Test
  public void createReturnsATinkerpopStorageIterator() throws StorageException {
    Iterator<Vertex> iterator = Lists.<Vertex> newArrayList().iterator();

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.create(ENTITY_TYPE, iterator);

    // verify
    assertThat(storageIterator, is(instanceOf(TINKERPOP_ITERATOR_TYPE)));
    verify(elementConverterFactory).forType(ENTITY_TYPE);
  }

  @Test
  public void createForRelationReturnsATinkerpopStorageIterator() {
    // setup
    Iterable<Edge> edges = Lists.newArrayList();

    // action
    StorageIterator<SubARelation> storageIterator = instance.createForRelation(RELATION_TYPE, edges);

    // verify
    assertThat(storageIterator, is(instanceOf(TINKERPOP_ITERATOR_TYPE)));
    verify(elementConverterFactory).forRelation(RELATION_TYPE);
  }
}
