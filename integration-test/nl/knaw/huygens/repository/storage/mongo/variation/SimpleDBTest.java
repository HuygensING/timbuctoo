package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;

import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.storage.mongo.variation.model.projecta.OtherDoc;
import nl.knaw.huygens.repository.variation.TestDoc;

public class SimpleDBTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test() throws IOException {
    StorageConfiguration conf = new StorageConfiguration("127.0.0.1", 27017, "integrationtest", "test", "test", "mongo");
    MongoModifiableVariationStorage s = new MongoModifiableVariationStorage(conf);
    try {
      TestDoc doc = new TestDoc();
      doc.name = "blub";
      doc.blah = "Floo";
      doc.setId("TST0001");
      s.addItem(doc , TestDoc.class);
      OtherDoc otherDoc = new OtherDoc();
      otherDoc.name = "blob";
      otherDoc.otherThing = "Flups";
      otherDoc.setId("TST0001");
      s.updateItem("TST0001", otherDoc, OtherDoc.class);
      TestDoc returnedItem = s.getItem("TST0001", TestDoc.class);
      assertEquals(new BasicDBObject("^rev", null), MongoDiff.diffDocuments(returnedItem, doc));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      s.db.getCollection("testbasedoc").remove(new BasicDBObject("_id", "TST0001"));
      s.destroy();
    }
    
  }

}
