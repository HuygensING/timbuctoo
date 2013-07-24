package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Reference;

import org.bson.BSONObject;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoDiffTest {

  public static class Foo extends Document {
    public static class Baz {
      public int x;
      public int y;
    }

    public String name;
    public List<String> bars;
    public Baz baz;

    public int blah;

    @Override
    public String getDisplayName() {
      return name;
    }

    @Override
    @JsonProperty("!currentVariation")
    public String getCurrentVariation() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    @JsonProperty("!currentVariation")
    public void setCurrentVariation(String defaultVRE) {
      // TODO Auto-generated method stub

    }

  }

  @Before
  public void setUp() throws Exception {

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

    DBObject xBSON = MongoUtils.getObjectForDoc(x);
    DBObject yBSON = MongoUtils.getObjectForDoc(y);
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

    DBObject xBSON = MongoUtils.getObjectForDoc(x);
    DBObject yBSON = MongoUtils.getObjectForDoc(y);
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

    DBObject xBSON = MongoUtils.getObjectForDoc(x);
    DBObject yBSON = MongoUtils.getObjectForDoc(y);
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

    DBObject xBSON = MongoUtils.getObjectForDoc(x);
    DBObject yBSON = MongoUtils.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(testObj, diff);
  }

  @Test
  public void testNewVariationsNull() throws IOException {
    Foo x = new Foo();
    x.setId("foo");
    Foo y = new Foo();
    y.setId("foo");
    x.bars = Lists.newArrayList("a", "b");
    x.setVariations(Lists.newArrayList(new Reference(Foo.class, "foo")));
    y.bars = Lists.newArrayList("a", "b", "c");
    y.setVariations(null);
    BSONObject diff = MongoDiff.diffDocuments(x, y);
    BSONObject testObj = new BasicDBObject();
    testObj.put("bars", Lists.newArrayList("a", "b", "c"));
    testObj.put("@variations", null);
    assertEquals(testObj, diff);

    DBObject xBSON = MongoUtils.getObjectForDoc(x);
    DBObject yBSON = MongoUtils.getObjectForDoc(y);
    diff = MongoDiff.diffToNewObject(xBSON, yBSON);
    assertEquals(testObj, diff);
  }
}
