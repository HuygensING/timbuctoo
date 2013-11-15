package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDiffTest.Foo;

import org.junit.Test;

import com.mongodb.BasicDBObject;

public class MongoUtilsTest {

  @Test
  public void testGetCollectionName() {
    assertEquals("foo", MongoUtils.getCollectionName(Foo.class));
  }

  @Test
  public void testGetVersioningCollectionName() {
    assertEquals("foo_versions", MongoUtils.getVersioningCollectionName(Foo.class));
  }

  @Test
  public void testGetObjectForDoc() {
    Foo x = new Foo();
    x.name = "blub";
    try {
      assertEquals(new BasicDBObject("name", "blub"), MongoUtils.getObjectForDoc(x));
    } catch (IOException e) {
      fail();
    }
  }

}
