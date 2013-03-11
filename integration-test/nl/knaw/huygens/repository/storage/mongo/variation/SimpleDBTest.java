package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.util.Configuration;
import nl.knaw.huygens.repository.variation.model.projecta.OtherDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

public class SimpleDBTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test() throws IOException {
    StorageConfiguration conf = new StorageConfiguration("127.0.0.1", 27017, "integrationtest", "test", "test", "mongo");
    Configuration config = mock(Configuration.class);
    DocumentTypeRegister docTypeRegistry = new DocumentTypeRegister(config.getSetting("model-packages"));
    MongoModifiableVariationStorage s = new MongoModifiableVariationStorage(conf, docTypeRegistry );
    s.db.getCollection("testbasedoc").drop();
    s.db.getCollection("testbasedoc-versions").drop();
    String docId = "TST0001";
    try {
      s.db.getCollection("testbasedoc").remove(new BasicDBObject("_id", docId));
    } catch (Exception ex) {
      System.err.println("Caught exception trying to remove item...");
      ex.printStackTrace();
    }

    try {
      TestDoc doc = new TestDoc();
      doc.name = "blub";
      doc.blah = "Floo";
      doc.setRev(0);
      doc.setId(docId);
      s.addItem(doc , TestDoc.class);
      OtherDoc otherDoc = new OtherDoc();
      otherDoc.name = "blob";
      otherDoc.otherThing = "Flups";
      otherDoc.setId(docId);
      otherDoc.setRev(0);
      s.updateItem(docId, otherDoc, OtherDoc.class);
      TestDoc returnedItem = s.getItem(docId, TestDoc.class);
      BasicDBObject expectedChange = new BasicDBObject("^rev", 1);
      assertEquals(expectedChange, MongoDiff.diffDocuments(doc, returnedItem));
      
      TestDoc doc2 = new TestDoc();
      doc2.name = "blubber";
      doc2.blah = "Floo";
      doc2.setId(docId);
      doc2.setRev(1);
      s.updateItem(docId, doc2, TestDoc.class);
      returnedItem = s.getItem(docId, TestDoc.class);
      expectedChange.put("^rev", 2);
      assertEquals(expectedChange, MongoDiff.diffDocuments(doc2, returnedItem));
      
      s.deleteItem(docId, TestDoc.class, null);
      expectedChange.put("^deleted", true);
      expectedChange.put("^rev", 3);
      expectedChange.put("name", "blubber");
      returnedItem = s.getItem(docId, TestDoc.class);
      assertEquals(expectedChange, MongoDiff.diffDocuments(doc, returnedItem));
    } catch (Exception ex) {
      ex.printStackTrace();
      fail();
    } finally {
      s.db.getCollection("testbasedoc").drop();
      s.db.getCollection("testbasedoc-versions").drop();
    }
    
  }

}
