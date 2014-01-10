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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

import com.google.common.base.Objects;

public class PersonNameComponentTest {

  @Test
  public void testEqualsEqual() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.FORENAME, "test");

    assertTrue(first.equals(second));
  }

  @Test
  public void testEqualsNoEqualType() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.SURNAME, "test");

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsNoEqualValue() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = new PersonNameComponent(Type.FORENAME, "test1");

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsToNull() {
    PersonNameComponent first = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent second = null;

    assertFalse(first.equals(second));
  }

  @Test
  public void testHashcode() {
    PersonNameComponent component = new PersonNameComponent(Type.FORENAME, "test");

    int expected = Objects.hashCode(Type.FORENAME, "test");

    assertEquals(expected, component.hashCode());

  }
}
