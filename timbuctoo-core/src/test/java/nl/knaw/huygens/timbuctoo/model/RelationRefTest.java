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

  private static final String RELATION_NAME = "relName";
  private static final int REVISION = 0;
  private static final boolean ACCEPTED = true;
  private static final String RELATION_ID = "relationId";
  private static final String NAME = "name";
  private static final String ID = "id";
  private static final String XTYPE = "xtype";
  private static final String TYPE = "type";

  @Test
  public void testDisplayName() {
    RelationRef ref = new RelationRef(TYPE, XTYPE, ID, null, RELATION_ID, ACCEPTED, REVISION, RELATION_NAME);
    assertEquals("", ref.getDisplayName());

    ref.setDisplayName(NAME);
    assertEquals(NAME, ref.getDisplayName());

    ref.setDisplayName(null);
    assertEquals("", ref.getDisplayName());
  }

  @Test
  public void testPath() {
    RelationRef ref = new RelationRef(TYPE, XTYPE, ID, NAME, RELATION_ID, ACCEPTED, 0, RELATION_NAME);

    assertEquals("domain/xtype/id", ref.getPath());
  }

  @Test
  public void testCompareToWithDifferentNames() {
    RelationRef ref1 = new RelationRef(TYPE, XTYPE, ID, "name1", RELATION_ID, ACCEPTED, 0, RELATION_NAME);
    RelationRef ref2 = new RelationRef(TYPE, XTYPE, ID, "name2", RELATION_ID, ACCEPTED, 0, RELATION_NAME);

    assertTrue(ref1.compareTo(ref1) == 0);
    assertTrue(ref1.compareTo(ref2) < 0);
    assertTrue(ref2.compareTo(ref1) > 0);
    assertTrue(ref2.compareTo(ref2) == 0);
  }

}
