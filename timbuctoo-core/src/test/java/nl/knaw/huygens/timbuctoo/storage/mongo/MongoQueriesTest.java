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
