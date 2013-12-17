package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;

import org.bson.BSONObject;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoDiffTest {

  public static class Foo extends DomainEntity {
    public static class Baz {
      public int x;
      public int y;
    }

    public String name;
    public List<String> bars;
    public Baz baz;

    public int blah;
    protected List<Reference> variations = Lists.newArrayList();

    @Override
    public String getDisplayName() {
      return name;
    }
  }

  @Test
  public void testGetObjectForDoc() {
    Foo x = new Foo();
    x.name = "blub";
    try {
      assertEquals(new BasicDBObject("name", "blub"), MongoDiff.getObjectForDoc(x));
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testDiff() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    BSONObject obj = MongoDiff.diffDocuments(x, y);
    assertEquals(null, obj);
  }

  @Test
  public void testDiffNested() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    Foo.Baz baz = new Foo.Baz();
    baz.x = 50;
    y.baz = baz;
    baz = new Foo.Baz();
    baz.y = 50;
    x.baz = baz;
    BSONObject obj = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    BSONObject bazObj = new BasicDBObject();
    bazObj.put("y", null);
    bazObj.put("x", 50);
    testObj.put("baz", bazObj);
    assertEquals(testObj, obj);
  }

  @Test
  public void testDiffNulls() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    Foo.Baz baz = new Foo.Baz();
    baz.x = 50;
    x.baz = baz;
    y.baz = new Foo.Baz();

    y.blah = 10;
    BSONObject obj = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("baz", new BasicDBObject("x", null));
    testObj.put("blah", 10);
    assertEquals(testObj, obj);
  }

  @Test
  public void testDiffNestedEqualObjects() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    Foo.Baz dim = new Foo.Baz();
    dim.x = 50;
    x.baz = dim;
    dim = new Foo.Baz();
    dim.x = 50;
    y.baz = dim;
    y.blah = 10;
    BSONObject obj = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("blah", 10);
    assertEquals(testObj, obj);
  }

  @Test
  public void testDiffUnequalPlainprops() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.blah = 5;
    y.blah = 10;
    BSONObject obj = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("blah", 10);
    assertEquals(testObj, obj);
  }

  @Test
  public void testLastListLonger() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.bars = Lists.newArrayList("a", "b");
    y.bars = Lists.newArrayList("a", "b", "c");
    BSONObject diff = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("bars", Lists.newArrayList("a", "b", "c"));
    assertEquals(testObj, diff);

    DBObject xBSON = MongoDiff.getObjectForDoc(x);
    DBObject yBSON = MongoDiff.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(testObj, diff);
  }

  @Test
  public void testFirstListLonger() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.bars = Lists.newArrayList("a", "b", "c");
    y.bars = Lists.newArrayList("a", "b");
    BSONObject diff = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("bars", Lists.newArrayList("a", "b"));
    assertEquals(testObj, diff);

    DBObject xBSON = MongoDiff.getObjectForDoc(x);
    DBObject yBSON = MongoDiff.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(testObj, diff);
  }

  @Test
  public void testListsInADifferentOrder() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.bars = Lists.newArrayList("a", "c", "b");
    y.bars = Lists.newArrayList("a", "b", "c");
    BSONObject diff = MongoDiff.diffDocuments(x, y);
    assertEquals(null, diff);

    DBObject xBSON = MongoDiff.getObjectForDoc(x);
    DBObject yBSON = MongoDiff.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(null, diff);
  }

  @Test
  public void testNewListIsNull() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.bars = Lists.newArrayList("a", "b");
    y.bars = null;
    BSONObject diff = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("bars", null);
    assertEquals(testObj, diff);

    DBObject xBSON = MongoDiff.getObjectForDoc(x);
    DBObject yBSON = MongoDiff.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(testObj, diff);
  }

}
