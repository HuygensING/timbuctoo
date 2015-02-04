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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Test;

public class PropertyNameTest {

  private void testPropertyNameFor(Class<? extends Entity> type) {
    MongoProperties properties = new MongoProperties();
    assertThat(properties.propertyName(type, "_x"), equalTo("_x"));
    assertThat(properties.propertyName(type, "^x"), equalTo("^x"));
    assertThat(properties.propertyName(type, "@x"), equalTo("@x"));
    assertThat(properties.propertyName(type, "xx"), equalTo(properties.propertyPrefix(type) + MongoProperties.SEPARATOR + "xx"));
  }

  @Test
  public void testPropertyNameForEntity() {
    testPropertyNameFor(Entity.class);
  }

  @Test
  public void testPropertyNameForSystemEntity() {
    testPropertyNameFor(SystemEntity.class);
  }

  @Test
  public void testPropertyNameForDomainEntity() {
    testPropertyNameFor(DomainEntity.class);
  }

  @Test
  public void testPropertyNameForUser() {
    testPropertyNameFor(User.class);
  }

  @Test
  public void testPropertyNameForPerson() {
    testPropertyNameFor(Person.class);
  }

}
