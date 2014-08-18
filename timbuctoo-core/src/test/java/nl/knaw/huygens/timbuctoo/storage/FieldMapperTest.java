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

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.SEPARATOR;
import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Before;
import org.junit.Test;

import test.variation.model.MongoObjectMapperEntity;

public class FieldMapperTest {

  private static final Class<? extends Entity> TYPE = MongoObjectMapperEntity.class;

  private FieldMapper fieldMapper;

  @Before
  public void setup() {
    fieldMapper = new FieldMapper();
  }

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

  private static class Foo {}

  @Test
  public void testGetFieldMap() {
    Map<String, String> map = fieldMapper.getFieldMap(Foo.class, TYPE);

    assertThat(map, hasEntry("primitiveTestCollection", propertyName(Foo.class, "primitiveTestCollection")));
    assertThat(map, hasEntry("nonPrimitiveTestCollection", propertyName(Foo.class, "nonPrimitiveTestCollection")));
    assertThat(map, hasEntry("name", propertyName(Foo.class, "name")));
    assertThat(map, hasEntry("testValue1", propertyName(Foo.class, "testValue1")));
    assertThat(map, hasEntry("testValue2", propertyName(Foo.class, "testValue2")));
    assertThat(map, hasEntry("annotatedProperty", propertyName(Foo.class, "propAnnotated")));
    assertThat(map, hasEntry("propWithAnnotatedAccessors", propertyName(Foo.class, "pwaa")));
    assertThat(map, hasEntry("type", propertyName(Foo.class, "type")));
    assertThat(map, hasEntry("date", propertyName(Foo.class, "date")));
    assertThat(map, hasEntry("personName", propertyName(Foo.class, "personName")));
  }

  @Test
  public void testGetFieldNameSimpleField() throws Exception {
    assertThat(fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("name")), equalTo("name"));
  }

  @Test
  public void testGetFieldNameForFieldWithAnnotation() throws Exception {
    assertThat(fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("annotatedProperty")), equalTo("propAnnotated"));
  }

  @Test
  public void testGetFieldNameFieldForAccessorWithAnnotation() throws Exception {
    assertThat(fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("propWithAnnotatedAccessors")), equalTo("pwaa"));
  }

  @Test
  public void testGetFieldNameForEntity() throws Exception {
    assertThat(fieldMapper.getFieldName(Entity.class, Entity.class.getDeclaredField("id")), equalTo("_id"));
  }

  @Test
  public void testGetFieldNameForDomainEntity() throws Exception {
    assertThat(fieldMapper.getFieldName(DomainEntity.class, Entity.class.getDeclaredField("id")), equalTo("_id"));
  }

  @Test
  public void testGetFieldNameForSystemEntity() throws Exception {
    assertThat(fieldMapper.getFieldName(SystemEntity.class, Entity.class.getDeclaredField("id")), equalTo("_id"));
  }

  @Test
  public void testGetTypeNameOfFieldNameWithSeparator() {
    String fieldName = propertyName("test", "testField");
    assertThat(fieldMapper.getTypeNameOfFieldName(fieldName), equalTo("test"));
  }

  @Test
  public void testGetTypeNameOfFieldNameWithoutSeparator() {
    String fieldName = "testField";
    assertThat(fieldMapper.getTypeNameOfFieldName(fieldName), is(nullValue()));
  }

  @Test(expected = NullPointerException.class)
  public void testGetTypeNameOfFieldNameNull() {
    fieldMapper.getTypeNameOfFieldName(null);
  }

}
