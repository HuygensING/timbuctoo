package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;

public class Neo4JStorageIteratorFactoryTest {
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;

  @Test
  public void createCreatesANeo4JStorageIterator() {
    Neo4JStorageIteratorFactory instance = new Neo4JStorageIteratorFactory();
    List<TestSystemEntityWrapper> entities = Lists.newArrayList();

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.create(SYSTEM_ENTITY_TYPE, entities);

    // verify
    assertThat(storageIterator, is(notNullValue()));
  }
}
