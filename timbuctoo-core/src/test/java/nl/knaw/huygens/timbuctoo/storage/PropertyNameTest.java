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

import static nl.knaw.huygens.timbuctoo.storage.FieldMap.SEPARATOR;
import static nl.knaw.huygens.timbuctoo.storage.FieldMap.propertyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Test;

public class PropertyNameTest {

  @Test
  public void testPropertyNameForEntity() {
    assertThat(propertyName(Entity.class, "_x"), equalTo("_x"));
    assertThat(propertyName(Entity.class, "^x"), equalTo("^x"));
    assertThat(propertyName(Entity.class, "@x"), equalTo("@x"));
    assertThat(propertyName(Entity.class, "xx"), equalTo("entity" + SEPARATOR + "xx"));
  }

  @Test
  public void testPropertyNameForSystemEntity() {
    assertThat(propertyName(SystemEntity.class, "_x"), equalTo("_x"));
    assertThat(propertyName(SystemEntity.class, "^x"), equalTo("^x"));
    assertThat(propertyName(SystemEntity.class, "@x"), equalTo("@x"));
    assertThat(propertyName(SystemEntity.class, "xx"), equalTo("systementity" + SEPARATOR + "xx"));
  }

  @Test
  public void testPropertyNameForDomainEntity() {
    assertThat(propertyName(DomainEntity.class, "_x"), equalTo("_x"));
    assertThat(propertyName(DomainEntity.class, "^x"), equalTo("^x"));
    assertThat(propertyName(DomainEntity.class, "@x"), equalTo("@x"));
    assertThat(propertyName(DomainEntity.class, "xx"), equalTo("domainentity" + SEPARATOR + "xx"));
  }

  @Test
  public void testPropertyNameForUser() {
    assertThat(propertyName(User.class, "_x"), equalTo("_x"));
    assertThat(propertyName(User.class, "^x"), equalTo("^x"));
    assertThat(propertyName(User.class, "@x"), equalTo("@x"));
    assertThat(propertyName(User.class, "xx"), equalTo("user" + SEPARATOR + "xx"));
  }

  @Test
  public void testPropertyNameForPerson() {
    assertThat(propertyName(Person.class, "_x"), equalTo("_x"));
    assertThat(propertyName(Person.class, "^x"), equalTo("^x"));
    assertThat(propertyName(Person.class, "@x"), equalTo("@x"));
    assertThat(propertyName(Person.class, "xx"), equalTo("person" + SEPARATOR + "xx"));
  }

}
