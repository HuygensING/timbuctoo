package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class PersonNameTest {

  @Test
  public void testEqualsEqual() {
    PersonName first = createPersonName("test", "test2");
    PersonName second = createPersonName("test", "test2");

    assertTrue(first.equals(second));
  }

  protected PersonName createPersonName(String forename, String surname) {
    PersonName personName = new PersonName();
    personName.addNameComponent(Type.FORENAME, forename);
    personName.addNameComponent(Type.SURNAME, surname);

    return personName;
  }

  @Test
  public void testEqualsNotEqual() {
    PersonName first = createPersonName("test", "test2");
    PersonName second = createPersonName("test1", "test2");

    assertFalse(first.equals(second));
  }

  @Test
  public void testHashCode() {
    PersonName personName = createPersonName("test", "test2");

    PersonNameComponent foreName = new PersonNameComponent(Type.FORENAME, "test");
    PersonNameComponent surname = new PersonNameComponent(Type.SURNAME, "test2");

    List<PersonNameComponent> components = Lists.newArrayList(foreName, surname);

    assertEquals(Objects.hashCode(components), personName.hashCode());
  }

}
