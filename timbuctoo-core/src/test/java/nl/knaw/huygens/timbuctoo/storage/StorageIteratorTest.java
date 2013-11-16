package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class StorageIteratorTest {

  private StorageIterator<String> iterator;

  @Before
  public void setupEmptyIterator() {
    iterator = new EmptyStorageIterator<String>();
  }

  @Test
  public void testEmptyIteratorHasNext() {
    assertFalse(iterator.hasNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptyIteratorNext() {
    iterator.next();
  }

  @Test
  public void testEmptyIteratorSize() {
    assertEquals(0, iterator.size());
  }

}
