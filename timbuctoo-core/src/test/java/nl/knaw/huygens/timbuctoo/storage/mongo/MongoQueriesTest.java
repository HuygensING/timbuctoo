package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

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
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("key1", "value1");
    expected.put("key2", new Date());

    DBObject query = queries.selectByProperties(expected);
    assertEquals(expected, query.toMap());
  }

}
