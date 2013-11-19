package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class FieldMapperTest {

  private static final Class<? extends Entity> TYPE = MongoObjectMapperEntity.class;

  private FieldMapper instance;

  @Before
  public void setUp() {
    instance = new FieldMapper();
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

  @Test
  public void testGetFieldMap() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("primitiveTestCollection", propertyName(TYPE, "primitiveTestCollection"));
    expected.put("nonPrimitiveTestCollection", propertyName(TYPE, "nonPrimitiveTestCollection"));
    expected.put("name", propertyName(TYPE, "name"));
    expected.put("testValue1", propertyName(TYPE, "testValue1"));
    expected.put("testValue2", propertyName(TYPE, "testValue2"));
    expected.put("annotatedProperty", propertyName(TYPE, "propAnnotated"));
    expected.put("propWithAnnotatedAccessors", propertyName(TYPE, "pwaa"));
    expected.put("type", propertyName(TYPE, "type"));
    expected.put("date", propertyName(TYPE, "date"));
    expected.put("personName", propertyName(TYPE, "personName"));

    assertEquals(expected, instance.getFieldMap(TYPE));
  }

  @Test
  public void testGetFieldNameSimpleField() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("name"), propertyName(TYPE, "name"));
  }

  @Test
  public void testGetFieldNameForFieldWithAnnotation() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("annotatedProperty"), propertyName(TYPE, "propAnnotated"));
  }

  @Test
  public void testGetFieldNameFieldForAccessorWithAnnotation() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("propWithAnnotatedAccessors"), propertyName(TYPE, "pwaa"));
  }

  @Test
  public void testGetFieldNameForEntity() throws NoSuchFieldException {
    testGetFieldName(Entity.class, Entity.class.getDeclaredField("id"), "_id");
  }

  @Test
  public void testGetFieldNameForDomainEntity() throws NoSuchFieldException {
    testGetFieldName(DomainEntity.class, Entity.class.getDeclaredField("id"), "_id");
  }

  @Test
  public void testGetFieldNameForSystemEntity() throws NoSuchFieldException {
    testGetFieldName(SystemEntity.class, Entity.class.getDeclaredField("id"), "_id");
  }

  protected void testGetFieldName(Class<? extends Entity> type, Field declaredField, String expected) throws NoSuchFieldException {
    String actual = instance.getFieldName(type, declaredField);
    assertEquals(expected, actual);
  }

  @Test
  public void testGetTypeNameOfFieldNameFieldNameWithDot() {
    String fieldName = propertyName("test", "testField");
    assertEquals("test", instance.getTypeNameOfFieldName(fieldName));
  }

  @Test
  public void testGetTypeNameOfFieldNameFieldNameWithoutDot() {
    String fieldName = "testField";
    assertNull(instance.getTypeNameOfFieldName(fieldName));
  }

  @Test(expected = NullPointerException.class)
  public void testGetTypeNameOfFieldNameFieldNameNull() {
    instance.getTypeNameOfFieldName(null);
  }

}
