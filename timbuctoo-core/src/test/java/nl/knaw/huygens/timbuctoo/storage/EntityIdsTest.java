package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.mongo.EntityIds;

import org.junit.Test;

public class EntityIdsTest {

  @Test
  public void testGetIdPrefix() {
    assertEquals(EntityIds.UNKNOWN_ID_PREFIX, EntityIds.getIDPrefix(null));
    assertEquals(EntityIds.UNKNOWN_ID_PREFIX, EntityIds.getIDPrefix(String.class));
    assertEquals(EntityIds.UNKNOWN_ID_PREFIX, EntityIds.getIDPrefix(Entity.class));
    assertEquals("PERS", EntityIds.getIDPrefix(Person.class));
    assertEquals("PERS", EntityIds.getIDPrefix(XPerson.class));
  }

  @Test
  public void testFormatEntityId() {
    assertEquals("PERS000000000001", EntityIds.formatEntityId(Person.class, 1));
    assertEquals("PERS000000001001", EntityIds.formatEntityId(Person.class, 1001));
    assertEquals("PERS002147483647", EntityIds.formatEntityId(Person.class, Integer.MAX_VALUE));
    assertEquals("PERS002147483648", EntityIds.formatEntityId(Person.class, Integer.MAX_VALUE + 1L));
  }

  // -------------------------------------------------------------------

  @IDPrefix("PERS")
  private static class Person extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class XPerson extends Person {}

}
