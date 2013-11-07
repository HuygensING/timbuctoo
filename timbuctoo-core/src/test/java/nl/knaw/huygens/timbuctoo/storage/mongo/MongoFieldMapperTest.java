package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperEntity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class MongoFieldMapperTest {

  private static final Class<MongoObjectMapperEntity> TYPE = MongoObjectMapperEntity.class;
  private MongoFieldMapper instance;

  @Before
  public void setUp() {
    instance = new MongoFieldMapper();
  }

  @After
  public void tearDown() {
    instance = null;
  }

  @Test
  public void testGetFieldMap() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("primitiveTestCollection", "mongoobjectmapperentity.primitiveTestCollection");
    expected.put("nonPrimitiveTestCollection", "mongoobjectmapperentity.nonPrimitiveTestCollection");
    expected.put("name", "mongoobjectmapperentity.name");
    expected.put("testValue1", "mongoobjectmapperentity.testValue1");
    expected.put("testValue2", "mongoobjectmapperentity.testValue2");
    expected.put("annotatedProperty", "mongoobjectmapperentity.propAnnotated");
    expected.put("propWithAnnotatedAccessors", "mongoobjectmapperentity.pwaa");

    Map<String, String> actual = instance.getFieldMap(MongoObjectMapperEntity.class);

    assertEquals(expected, actual);
  }

  @Test
  public void testGetFieldMapDomainEntity() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("pid", "^pid");
    expected.put("relations", "@relations");
    expected.put("variations", "@variations");
    expected.put("currentVariation", "!currentVariation");
    expected.put("roles", "roles");

    Map<String, String> actual = instance.getFieldMap(DomainEntity.class);

    assertEquals(expected, actual);
  }

  @Test
  public void testGetFieldMapSystemEntity() {
    Map<String, String> expected = Maps.newHashMap();

    Map<String, String> actual = instance.getFieldMap(SystemEntity.class);

    assertEquals(expected, actual);
  }

  @Test
  public void testGetFieldMapEntity() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("id", "_id");
    expected.put("rev", "^rev");
    expected.put("deleted", "^deleted");
    expected.put("creation", "^creation");
    expected.put("lastChange", "^lastChange");

    Map<String, String> actual = instance.getFieldMap(Entity.class);

    assertEquals(expected, actual);
  }

  @Test
  public void testGetFieldNameSimpleField() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("name"), "mongoobjectmapperentity.name");
  }

  @Test
  public void testGetFieldNameForFieldWithAnnotation() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("annotatedProperty"), "mongoobjectmapperentity.propAnnotated");
  }

  @Test
  public void testGetFieldNameFieldForAccessorWithAnnotation() throws NoSuchFieldException {
    testGetFieldName(TYPE, TYPE.getDeclaredField("propWithAnnotatedAccessors"), "mongoobjectmapperentity.pwaa");
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
}
