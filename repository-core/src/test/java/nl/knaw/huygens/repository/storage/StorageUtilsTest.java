package nl.knaw.huygens.repository.storage;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.model.Document;

import org.junit.Test;

public class StorageUtilsTest {

  @Test
  public void testGetIdPrefix() {
    assertEquals(StorageUtils.UNKNOWN_ID_PREFIX, StorageUtils.getIDPrefix(null));
    assertEquals(StorageUtils.UNKNOWN_ID_PREFIX, StorageUtils.getIDPrefix(String.class));
    assertEquals(StorageUtils.UNKNOWN_ID_PREFIX, StorageUtils.getIDPrefix(Document.class));
    assertEquals("PERS", StorageUtils.getIDPrefix(Person.class));
    assertEquals("PERS", StorageUtils.getIDPrefix(XPerson.class));
  }

  @Test
  public void testFormatEntityId() {
    assertEquals("PERS000000000001", StorageUtils.formatEntityId(Person.class, 1));
    assertEquals("PERS000000001001", StorageUtils.formatEntityId(Person.class, 1001));
    assertEquals("PERS002147483647", StorageUtils.formatEntityId(Person.class, Integer.MAX_VALUE));
    assertEquals("PERS002147483648", StorageUtils.formatEntityId(Person.class, Integer.MAX_VALUE + 1L));
  }

  // -------------------------------------------------------------------

  @IDPrefix("PERS")
  private static class Person extends Document {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class XPerson extends Person {}

}
