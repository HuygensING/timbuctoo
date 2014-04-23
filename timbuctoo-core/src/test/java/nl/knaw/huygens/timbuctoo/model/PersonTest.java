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

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Assert;
import org.junit.Test;

public class PersonTest {

  private PersonName createPersonName(String forename, String surname) {
    PersonName name = new PersonName();
    name.addNameComponent(Type.FORENAME, forename);
    name.addNameComponent(Type.SURNAME, surname);
    return name;
  }

  @Test
  public void testGetIndexedNameForNoName() {
   Person person = new Person();
   Assert.assertEquals("", person.getIndexedName());
  }

  @Test
  public void testGetIndexedNameForOneName() {
   Person person = new Person();
   person.addName(createPersonName("Christiaan", "Huygens"));
   Assert.assertEquals("Christiaan Huygens", person.getIndexedName());
  }

  @Test
  public void testGetIndexedNameForTwoNames() {
   Person person = new Person();
   person.addName(createPersonName("Christiaan", "Huygens"));
   person.addName(createPersonName("", "Archimedes"));
   Assert.assertEquals("Christiaan Huygens Archimedes", person.getIndexedName());
  }

  @Test
  public void testGetSortName() {
   Person person = new Person();
   person.addName(createPersonName("Christiaan", "Huygens"));
   Assert.assertEquals("Huygens, Christiaan", person.getSortName());
  }

}
