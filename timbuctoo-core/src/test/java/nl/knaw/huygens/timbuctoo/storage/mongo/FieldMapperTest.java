package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.mongo.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperEntity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

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
  public void testGetFieldMap() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("primitiveTestCollection", propertyName(TYPE, "primitiveTestCollection"));
    expected.put("nonPrimitiveTestCollection", propertyName(TYPE, "nonPrimitiveTestCollection"));
    expected.put("name", propertyName(TYPE, "name"));
    expected.put("testValue1", propertyName(TYPE, "testValue1"));
    expected.put("testValue2", propertyName(TYPE, "testValue2"));
    expected.put("annotatedProperty", propertyName(TYPE, "propAnnotated"));
    expected.put("propWithAnnotatedAccessors", propertyName(TYPE, "pwaa"));
    expected.put("date", propertyName(TYPE, "date"));
    expected.put("personName", propertyName(TYPE, "personName"));

    assertEquals(expected, instance.getFieldMap(TYPE));
  }

  @Test
  public void testGetFieldMapDomainEntity() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("pid", DomainEntity.PID);
    expected.put("relations", "@relations");
    expected.put("variations", "@variations");
    expected.put("currentVariation", "!currentVariation");
    expected.put("roles", "roles");

    assertEquals(expected, instance.getFieldMap(DomainEntity.class));
  }

  @Test
  public void testGetFieldMapSystemEntity() {
    Map<String, String> expected = Maps.newHashMap();

    assertEquals(expected, instance.getFieldMap(SystemEntity.class));
  }

  @Test
  public void testGetFieldMapEntity() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("id", "_id");
    expected.put("rev", "^rev");
    expected.put("deleted", "^deleted");
    expected.put("creation", "^creation");
    expected.put("lastChange", "^lastChange");

    assertEquals(expected, instance.getFieldMap(Entity.class));
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
