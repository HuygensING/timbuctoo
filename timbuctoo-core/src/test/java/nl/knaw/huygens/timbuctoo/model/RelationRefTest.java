package nl.knaw.huygens.timbuctoo.model;

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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RelationRefTest {

  @Test
  public void testDisplayName() {
    RelationRef ref = new RelationRef("type", "xtype", "id", null, "relationId", true, 0);
    assertEquals("", ref.getDisplayName());

    ref.setDisplayName("name");
    assertEquals("name", ref.getDisplayName());

    ref.setDisplayName(null);
    assertEquals("", ref.getDisplayName());
  }

  @Test
  public void testPath() {
    RelationRef ref = new RelationRef("type", "xtype", "id", "name", "relationId", true, 0);

    assertEquals("domain/xtype/id", ref.getPath());
  }

  @Test
  public void testCompareTo() {
    RelationRef ref1 = new RelationRef("type", "xtype", "id", "name1", "relationId", true, 0);
    RelationRef ref2 = new RelationRef("type", "xtype", "id", "name2", "relationId", true, 0);

    assertTrue(ref1.compareTo(ref1) == 0);
    assertTrue(ref1.compareTo(ref2) < 0);
    assertTrue(ref2.compareTo(ref1) > 0);
    assertTrue(ref2.compareTo(ref2) == 0);
  }

}
