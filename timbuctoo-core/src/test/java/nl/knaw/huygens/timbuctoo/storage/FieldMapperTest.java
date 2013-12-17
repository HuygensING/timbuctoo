package nl.knaw.huygens.timbuctoo.storage;

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

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.variation.model.MongoObjectMapperEntity;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class FieldMapperTest {

  private static final Class<? extends Entity> TYPE = MongoObjectMapperEntity.class;

  private FieldMapper fieldMapper;

  @Before
  public void setup() {
    fieldMapper = new FieldMapper();
  }

  @Test
  public void testPropertyNameForEntity() {
    assertEquals("_x", propertyName(Entity.class, "_x"));
    assertEquals("^x", propertyName(Entity.class, "^x"));
    assertEquals("@x", propertyName(Entity.class, "@x"));
    assertEquals("entity" + FieldMapper.SEPARATOR + "xx", propertyName(Entity.class, "xx"));
  }

  @Test
  public void testPropertyNameForSystemEntity() {
    assertEquals("_x", propertyName(SystemEntity.class, "_x"));
    assertEquals("^x", propertyName(SystemEntity.class, "^x"));
    assertEquals("@x", propertyName(SystemEntity.class, "@x"));
    assertEquals("systementity" + FieldMapper.SEPARATOR + "xx", propertyName(SystemEntity.class, "xx"));
  }

  @Test
  public void testPropertyNameForDomainEntity() {
    assertEquals("_x", propertyName(DomainEntity.class, "_x"));
    assertEquals("^x", propertyName(DomainEntity.class, "^x"));
    assertEquals("@x", propertyName(DomainEntity.class, "@x"));
    assertEquals("domainentity" + FieldMapper.SEPARATOR + "xx", propertyName(DomainEntity.class, "xx"));
  }

  @Test
  public void testPropertyNameForUser() {
    assertEquals("_x", propertyName(User.class, "_x"));
    assertEquals("^x", propertyName(User.class, "^x"));
    assertEquals("@x", propertyName(User.class, "@x"));
    assertEquals("user" + FieldMapper.SEPARATOR + "xx", propertyName(User.class, "xx"));
  }

  @Test
  public void testPropertyNameForPerson() {
    assertEquals("_x", propertyName(Person.class, "_x"));
    assertEquals("^x", propertyName(Person.class, "^x"));
    assertEquals("@x", propertyName(Person.class, "@x"));
    assertEquals("person" + FieldMapper.SEPARATOR + "xx", propertyName(Person.class, "xx"));
  }

  private static class Foo {}

  @Test
  public void testGetFieldMap() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("primitiveTestCollection", propertyName(Foo.class, "primitiveTestCollection"));
    expected.put("nonPrimitiveTestCollection", propertyName(Foo.class, "nonPrimitiveTestCollection"));
    expected.put("name", propertyName(Foo.class, "name"));
    expected.put("testValue1", propertyName(Foo.class, "testValue1"));
    expected.put("testValue2", propertyName(Foo.class, "testValue2"));
    expected.put("annotatedProperty", propertyName(Foo.class, "propAnnotated"));
    expected.put("propWithAnnotatedAccessors", propertyName(Foo.class, "pwaa"));
    expected.put("type", propertyName(Foo.class, "type"));
    expected.put("date", propertyName(Foo.class, "date"));
    expected.put("personName", propertyName(Foo.class, "personName"));

    assertEquals(expected, fieldMapper.getFieldMap(Foo.class, TYPE));
  }

  @Test
  public void testGetFieldNameSimpleField() throws Exception {
    assertEquals("name", fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("name")));
  }

  @Test
  public void testGetFieldNameForFieldWithAnnotation() throws Exception {
    assertEquals("propAnnotated", fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("annotatedProperty")));
  }

  @Test
  public void testGetFieldNameFieldForAccessorWithAnnotation() throws Exception {
    assertEquals("pwaa", fieldMapper.getFieldName(TYPE, TYPE.getDeclaredField("propWithAnnotatedAccessors")));
  }

  @Test
  public void testGetFieldNameForEntity() throws Exception {
    assertEquals("_id", fieldMapper.getFieldName(Entity.class, Entity.class.getDeclaredField("id")));
  }

  @Test
  public void testGetFieldNameForDomainEntity() throws Exception {
    assertEquals("_id", fieldMapper.getFieldName(DomainEntity.class, Entity.class.getDeclaredField("id")));
  }

  @Test
  public void testGetFieldNameForSystemEntity() throws Exception {
    assertEquals("_id", fieldMapper.getFieldName(SystemEntity.class, Entity.class.getDeclaredField("id")));
  }

  @Test
  public void testGetTypeNameOfFieldNameWithSeparator() {
    String fieldName = propertyName("test", "testField");
    assertEquals("test", fieldMapper.getTypeNameOfFieldName(fieldName));
  }

  @Test
  public void testGetTypeNameOfFieldNameWithoutSeparator() {
    String fieldName = "testField";
    assertNull(fieldMapper.getTypeNameOfFieldName(fieldName));
  }

  @Test(expected = NullPointerException.class)
  public void testGetTypeNameOfFieldNameNull() {
    fieldMapper.getTypeNameOfFieldName(null);
  }

}
