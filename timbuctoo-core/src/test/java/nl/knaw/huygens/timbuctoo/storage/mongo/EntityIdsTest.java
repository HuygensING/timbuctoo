package nl.knaw.huygens.timbuctoo.storage.mongo;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBCollection;

public class EntityIdsTest {

  private EntityIds entityIds;

  @Before
  public void setup() {
    MongoDB mongoDB = mock(MongoDB.class);
    DBCollection dbCollection = mock(DBCollection.class);
    when(mongoDB.getCollection(EntityIds.ID_COLLECTION_NAME)).thenReturn(dbCollection);
    entityIds = new EntityIds(mongoDB);
  }

  @Test
  public void testGetIdPrefix() {
    assertEquals(EntityIds.UNKNOWN_ID_PREFIX, entityIds.getIDPrefix(null));
    assertEquals(EntityIds.UNKNOWN_ID_PREFIX, entityIds.getIDPrefix(Entity.class));
    assertEquals("LANG", entityIds.getIDPrefix(Language.class));
    assertEquals("LANG", entityIds.getIDPrefix(BaseLanguage.class));
  }

  @Test
  public void testFormatEntityId() {
    assertEquals("LANG000000000001", entityIds.formatEntityId(Language.class, 1));
    assertEquals("LANG000000001001", entityIds.formatEntityId(Language.class, 1001));
    assertEquals("LANG002147483647", entityIds.formatEntityId(Language.class, Integer.MAX_VALUE));
    assertEquals("LANG002147483648", entityIds.formatEntityId(Language.class, Integer.MAX_VALUE + 1L));
  }

}
