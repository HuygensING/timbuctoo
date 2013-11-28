package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntity;

import com.google.common.collect.Maps;
import com.mongodb.DBObject;

public class MongoQueriesTest {

  private MongoQueries queries;

  @Before
  public void setupMongoQueries() {
    queries = new MongoQueries();
  }

  @Test
  public void testSelectAll() {
    Map<String, Object> expected = Maps.newHashMap();

    DBObject query = queries.selectAll();
    assertEquals(expected, query.toMap());
  }

  @Test
  public void testSelectById() {
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("_id", "testId");

    DBObject query = queries.selectById("testId");
    assertEquals(expected, query.toMap());
  }

  @Test
  public void testSelectByProperty() {
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("testKey", "testValue");

    DBObject query = queries.selectByProperty("testKey", "testValue");
    assertEquals(expected, query.toMap());
  }

  @Test
  public void testSelectByProperties() {
    TestSystemEntity entity = new TestSystemEntity("id", "v1", "v2");
    DBObject query = queries.selectByProperties(TestSystemEntity.class, entity);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(FieldMapper.propertyName(TestSystemEntity.class, "value1"), "v1");
    expected.put(FieldMapper.propertyName(TestSystemEntity.class, "value2"), "v2");

    assertEquals(expected, query.toMap());
  }

}
