package nl.knaw.huygens.timbuctoo.model.util;

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
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Test;

public class PersonNameBuilderTest {

  @Test
  public void testSeparator() {
    assertEquals("", PersonNameBuilder.separator(null, null));
    assertEquals("", PersonNameBuilder.separator(Type.FORENAME, null));
    assertEquals("", PersonNameBuilder.separator(null, Type.SURNAME));
    assertEquals(" ", PersonNameBuilder.separator(Type.FORENAME, Type.SURNAME));
    assertEquals(", ", PersonNameBuilder.separator(Type.SURNAME, Type.FORENAME));
  }

  @Test
  public void testOneComponent() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    assertEquals("Christiaan", builder.getName());
  }

  @Test
  public void testTwoComponents() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    builder.addComponent(new PersonNameComponent(Type.SURNAME, "Huygens"));
    assertEquals("Christiaan Huygens", builder.getName());
  }

  @Test
  public void testTwoComponentsReversed() {
    PersonNameBuilder builder = new PersonNameBuilder();
    builder.addComponent(new PersonNameComponent(Type.SURNAME, "Huygens"));
    builder.addComponent(new PersonNameComponent(Type.FORENAME, "Christiaan"));
    assertEquals("Huygens, Christiaan", builder.getName());
  }

}
