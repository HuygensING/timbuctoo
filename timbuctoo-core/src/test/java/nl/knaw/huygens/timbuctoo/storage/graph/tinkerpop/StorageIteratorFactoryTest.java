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

import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class StorageIteratorFactoryTest {
  @Test
  public void createReturnsATinkerpopStorageIterator() throws StorageException {
    // setup
    ElementConverterFactory elementConverterFactory = mock(ElementConverterFactory.class);
    StorageIteratorFactory instance = new StorageIteratorFactory(elementConverterFactory);
    Iterator<Vertex> iterator = Lists.<Vertex> newArrayList().iterator();

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.create(TestSystemEntityWrapper.class, iterator);

    // verify
    assertThat(storageIterator, is(instanceOf(TinkerpopIterator.class)));
    verify(elementConverterFactory).forType(TestSystemEntityWrapper.class);
  }
}
