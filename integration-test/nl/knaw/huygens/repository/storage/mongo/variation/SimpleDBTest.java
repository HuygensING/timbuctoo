package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.ProjectBGeneralTestDoc;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;

public class SimpleDBTest {

  @Test
  public void test() throws IOException {
    StorageConfiguration conf = new StorageConfiguration("127.0.0.1", 27017, "integrationtest", "test", "test", "mongo");
    Configuration config = mock(Configuration.class);
    DocTypeRegistry docTypeRegistry = new DocTypeRegistry(config.getSetting("model-packages"));
    MongoModifiableVariationStorage s = new MongoModifiableVariationStorage(conf, docTypeRegistry);
    s.db.getCollection("testconcretedoc").drop();
    s.db.getCollection("testconcretedoc-versions").drop();
    String docId = "TST0001";
    try {
      s.db.getCollection("testconcretedoc").remove(new BasicDBObject("_id", docId));
    } catch (Exception ex) {
      System.err.println("Caught exception trying to remove item...");
      ex.printStackTrace();
    }

    try {
      ProjectBGeneralTestDoc doc = new ProjectBGeneralTestDoc();
      doc.name = "blub";
      doc.projectBGeneralTestDocValue = "Floo";
      doc.setRev(0);
      doc.setId(docId);
      s.addItem(ProjectBGeneralTestDoc.class, doc);
      ProjectAGeneralTestDoc otherDoc = new ProjectAGeneralTestDoc();
      otherDoc.name = "blob";
      otherDoc.projectAGeneralTestDocValue = "Flups";
      otherDoc.setId(docId);
      otherDoc.setRev(0);
      s.updateItem(ProjectAGeneralTestDoc.class, docId, otherDoc);
      ProjectBGeneralTestDoc returnedItem = s.getItem(ProjectBGeneralTestDoc.class, docId);
      BasicDBObject expectedChange = new BasicDBObject();
      expectedChange.append("^rev", 1);
      //@variations are added by the VariationReducer
      expectedChange.append("@variations", Lists.newArrayList("projectb-projectbgeneraltestdoc", "generaltestdoc", "testconcretedoc", "projecta-projectageneraltestdoc"));

      assertEquals(expectedChange, MongoDiff.diffDocuments(doc, returnedItem));

      ProjectBGeneralTestDoc doc2 = new ProjectBGeneralTestDoc();
      doc2.name = "blubber";
      doc2.projectBGeneralTestDocValue = "Floo";
      doc2.setId(docId);
      doc2.setRev(1);
      s.updateItem(ProjectBGeneralTestDoc.class, docId, doc2);
      returnedItem = s.getItem(ProjectBGeneralTestDoc.class, docId);
      expectedChange.put("^rev", 2);
      assertEquals(expectedChange, MongoDiff.diffDocuments(doc2, returnedItem));

      s.deleteItem(ProjectBGeneralTestDoc.class, docId, null);
      expectedChange.put("^deleted", true);
      expectedChange.put("^rev", 3);
      expectedChange.put("name", "blubber");
      returnedItem = s.getItem(ProjectBGeneralTestDoc.class, docId);
      assertEquals(expectedChange, MongoDiff.diffDocuments(doc, returnedItem));
    } catch (Exception ex) {
      ex.printStackTrace();
      fail();
    } finally {
      s.db.getCollection("testconcretedoc").drop();
      s.db.getCollection("testconcretedoc-versions").drop();
    }

  }

}
