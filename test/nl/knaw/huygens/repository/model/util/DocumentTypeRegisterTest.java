package nl.knaw.huygens.repository.model.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.variation.model.TestBaseDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

public class DocumentTypeRegisterTest {

  private DocumentTypeRegister registry;

  @Before
  public void setUp() throws Exception {
    registry = new DocumentTypeRegister();
  }

  @Test
  public void test() {
    registry.registerPackageFromClass(TestBaseDoc.class);
    registry.registerPackageFromClass(TestDoc.class);
    assertEquals("Should work for document", "document", registry.getTypeString(Document.class));
    assertEquals("Should work for base doc", "testbasedoc", registry.getTypeString(TestBaseDoc.class));
    assertEquals("Should work for test doc", "testdoc", registry.getTypeString(TestDoc.class));
  }

}
