package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
