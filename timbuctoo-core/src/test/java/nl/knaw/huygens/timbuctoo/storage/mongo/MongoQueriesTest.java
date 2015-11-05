package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
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
    expected.put("completePropertyName", "testValue");

    DBObject query = queries.selectByProperty("completePropertyName", "testValue");
    assertEquals(expected, query.toMap());
  }

  @Test
  public void testSelectVersionByIdAndRevision() {
    int revision = 2;
    String expected = String.format("{ \"versions\" : { \"$elemMatch\" : { \"^rev\" : %d}}}", revision);

    DBObject query = queries.getRevisionProjection(revision);

    String actual = query.toString();

    /* 
     * Ignore all the whitespaces. IsEqualIgnoringWhiteSpace cannot be used, because it ignores only the 
     * one before and after the string and converts multiple whitespaces to a single space.
     */
    assertThat(actual.replaceAll(" ", ""), equalTo(expected.replaceAll(" ", "")));
  }

  @Test
  public void testSelectByModifiedDate() {
    // setup
    Date dateValue = new Date();
    String expectedQuery = String.format("{\"^modified.timeStamp\":{\"$lt\":%d}}", dateValue.getTime());

    // action
    DBObject query = queries.selectByModifiedDate(dateValue);

    // verify
    assertThat(toStringWithoutWhiteSpaces(query), is(equalTo(expectedQuery.replaceAll(" ", ""))));

  }

  private String toStringWithoutWhiteSpaces(DBObject query) {
    return query.toString().replaceAll("\\s", "");
  }

  @Test
  public void testSetPropertiesToValue() {
    // setup
    String propertyName1 = "object:accepted";
    boolean value1 = false;
    String propertyName2 = "object:otherProperty";
    String value2 = "value";

    Map<String, Object> propertiesWithValues = Maps.newHashMap();
    propertiesWithValues.put(propertyName1, value1);
    propertiesWithValues.put(propertyName2, value2);

    // action
    DBObject updateQuery = queries.setPropertiesToValue(propertiesWithValues);

    DBObject expectedProperties = new BasicDBObject().append(propertyName1, value1).append(propertyName2, value2);
    DBObject expected = new BasicDBObject("$set", expectedProperties);

    assertThat(updateQuery, is(equalTo(expected)));
  }
}
