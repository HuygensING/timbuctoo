package nl.knaw.huygens.repository.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collection;

import nl.knaw.huygens.repository.facet.CustomIndexer;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.facet.CustomIndexer.NoopIndexer;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.index.model.TestExtraBaseDoc;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Test;

public class SolrInputDocGeneratorTest {

  private int id = 0;

  private Entity createDocument(String displayName) {
    id++;
    return createDocument(displayName, "" + id);
  }

  private Entity createDocument(String displayName, String id) {
    TestExtraBaseDoc doc = mock(TestExtraBaseDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn(displayName);
    return doc;
  }

  private IndexAnnotation createIndexAnnotation(boolean canBeEmpty, String fieldName) {
    IndexAnnotation indexAnnotation = mock(IndexAnnotation.class);
    when(indexAnnotation.canBeEmpty()).thenReturn(canBeEmpty);
    when(indexAnnotation.fieldName()).thenReturn(fieldName);
    Class<? extends CustomIndexer> indexer = NoopIndexer.class;
    // type unsafe doReturn is needed, because when and thenReturn give problems
    // with generics.
    doReturn(indexer).when(indexAnnotation).customIndexer();
    when(indexAnnotation.isComplex()).thenReturn(false);
    when(indexAnnotation.isFaceted()).thenReturn(false);
    when(indexAnnotation.accessors()).thenReturn(new String[] {});

    return indexAnnotation;
  }

  private void processMethod(Entity doc, SolrInputDocGenerator generator, String methodName, boolean fieldCanBeEmpty, String fieldName) throws NoSuchMethodException {
    Method method = doc.getClass().getMethod(methodName);
    IndexAnnotation annotation = this.createIndexAnnotation(fieldCanBeEmpty, fieldName);
    generator.process(method, annotation);
  }

  @Test
  public void testGetResultOneDocumentWithoutEmptyFields() throws NoSuchMethodException, SecurityException {
    String expected = "test";
    Entity doc = createDocument(expected);
    SolrInputDocGenerator generator = new SolrInputDocGenerator(doc);

    processMethod(doc, generator, "getDisplayName", false, "desc");

    SolrInputDocument solrInputDocument = generator.getResult();
    SolrInputField field = solrInputDocument.getField("desc");
    Object actual = field.getFirstValue();

    assertEquals(expected, actual);
  }

  @Test
  public void testGetResultOneDocumentWithEmptyFieldsThatShouldBeIndexed() throws NoSuchMethodException {
    String description = null;
    String expected = "(empty)";
    Entity doc = createDocument(description);
    SolrInputDocGenerator generator = new SolrInputDocGenerator(doc);

    processMethod(doc, generator, "getDisplayName", false, "desc");

    SolrInputDocument solrInputDocument = generator.getResult();
    SolrInputField field = solrInputDocument.getField("desc");
    Object actual = field.getFirstValue();

    assertEquals(expected, actual);
  }

  @Test
  public void testGetResultOneDocument_WithEmptyFieldsThatShouldNotBeIndexed() throws NoSuchMethodException {
    Entity doc = createDocument(null);
    SolrInputDocGenerator generator = new SolrInputDocGenerator(doc);

    processMethod(doc, generator, "getDisplayName", true, "desc");

    SolrInputDocument solrInputDocument = generator.getResult();
    SolrInputField descriptionField = solrInputDocument.getField("desc");

    assertNull(descriptionField);
  }

  @Test
  public void testGetResultMultipleDocumentsWithoutEmptyFields() throws NoSuchMethodException {
    String descriptionDoc1 = "doc1";
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = "doc2";
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("desc");
    Collection<Object> actualDescriptions = descriptionField.getValues();

    assertEquals(2, actualDescriptions.size());
    assertTrue(actualDescriptions.contains(descriptionDoc1));
    assertTrue(actualDescriptions.contains(descriptionDoc2));
  }

  @Test
  public void testGetResultMultipleDocumentsWithEmptyFieldsThatShouldBeIndexedOneEmpty() throws NoSuchMethodException {
    String descriptionDoc1 = "doc1";
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = null;
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("desc");
    Collection<Object> actualValues = descriptionField.getValues();

    assertEquals(1, actualValues.size());
    assertTrue(actualValues.contains(descriptionDoc1));
  }

  @Test
  public void testGetResultMultipleDocumentsWithEmptyFieldsThatShouldBeIndexedAllEmpty() throws NoSuchMethodException {
    String descriptionDoc1 = null;
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = null;
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("desc");
    Collection<Object> actualValues = descriptionField.getValues();

    assertEquals(1, actualValues.size());
    assertTrue(actualValues.contains("(empty)"));
  }

  @Test
  public void testGetResultMultipleDocumentsWithEmptyFieldsThatShouldNotBeIndexed() throws NoSuchMethodException {
    String descriptionDoc1 = null;
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", true, "desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = null;
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", true, "desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("desc");

    assertNull(descriptionField);
  }

  @Test
  public void testGetResultMultipleDocumentsWithDuplicateValues() throws NoSuchMethodException {
    String description = "doc";
    Entity doc1 = createDocument(description);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    Entity doc2 = createDocument(description, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", false, "desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("desc");
    Collection<Object> actualValues = descriptionField.getValues();

    assertEquals(1, actualValues.size());
    assertTrue(actualValues.contains(description));
  }

  @Test
  public void testGetResultMultipleDocumentsIdField() throws NoSuchMethodException {
    String descriptionDoc1 = "doc1";
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getId", false, "_id");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = "doc2";
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getId", false, "_id");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("_id");
    Collection<Object> actualIds = descriptionField.getValues();

    assertEquals(1, actualIds.size());
    assertTrue(actualIds.contains(doc1.getId()));
  }

  @Test
  public void testGetResultMultipleDocumentsSortingField() throws NoSuchMethodException {
    String descriptionDoc1 = "doc1";
    Entity doc1 = createDocument(descriptionDoc1);
    SolrInputDocGenerator doc1Generator = new SolrInputDocGenerator(doc1);
    processMethod(doc1, doc1Generator, "getDisplayName", false, "dynamic_sort_desc");
    SolrInputDocument inputDoc1 = doc1Generator.getResult();

    String descriptionDoc2 = "doc2";
    Entity doc2 = createDocument(descriptionDoc2, doc1.getId());
    SolrInputDocGenerator doc2Generator = new SolrInputDocGenerator(doc2, inputDoc1);
    processMethod(doc2, doc2Generator, "getDisplayName", false, "dynamic_sort_desc");
    SolrInputDocument inputDoc2 = doc1Generator.getResult();

    SolrInputField descriptionField = inputDoc2.getField("dynamic_sort_desc");
    Collection<Object> actualIds = descriptionField.getValues();

    assertEquals(1, actualIds.size());
    assertTrue(actualIds.contains(doc1.getDisplayName()));
  }

}
