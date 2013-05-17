package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.ProjectBGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

import org.junit.Before;
import org.junit.Test;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;

public class VariationReducerTest {

  private VariationReducer reducer;
  private ObjectMapper m;
  private DocTypeRegistry docTypeRegistry;

  @Before
  public void setUp() {
    m = new ObjectMapper();
    docTypeRegistry = mock(DocTypeRegistry.class);
    reducer = new VariationReducer(docTypeRegistry);
  }

  @Test
  public void testReduce() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"blub\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}], \"!defaultVRE\":\"blub\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    ProjectAGeneralTestDoc val = reducer.reduce(t, ProjectAGeneralTestDoc.class);
    ProjectAGeneralTestDoc testVal = new ProjectAGeneralTestDoc();
    testVal.name = "a";
    testVal.generalTestDocValue = "a";
    testVal.projectAGeneralTestDocValue = "test";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceCommonDataOnly() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class);
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceCommonDataMultipleRoles() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class);
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "b";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "projectb-projectbgeneraltestdoc"));
    testVal.setCurrentVariation("projectb");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceRolDataAndCommonData() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    GeneralTestDoc val = reducer.reduce(t, GeneralTestDoc.class);
    GeneralTestDoc testVal = new GeneralTestDoc();
    testVal.name = "a";
    testVal.generalTestDocValue = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceRoleDataAndCommonDataMultipleRoles() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    GeneralTestDoc val = reducer.reduce(t, GeneralTestDoc.class);
    GeneralTestDoc testVal = new GeneralTestDoc();
    testVal.name = "a";
    testVal.generalTestDocValue = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "projectb-projectbgeneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceMissingRole() throws IOException {
    String x = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}],\"!defaultVRE\":\"blub\"}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    testVal.setVariations(Lists.newArrayList("testinheritsfromtestbasedoc"));
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  //Tests with explicitly requested variation.
  @Test
  public void testReduceCommonDataOnlyWithRequestedVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"blub-blubgeneraltestdoc\":{\"blubGeneralTestDocValue\":\"blubtest\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class, "blub");
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "b";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "blub-blubgeneraltestdoc"));
    testVal.setCurrentVariation("blub");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceCommonDataMultipleRolesWithRequestedVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class, "projecta");
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "projectb-projectbgeneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceRolDataAndCommonDataWithRequestedVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    GeneralTestDoc val = reducer.reduce(t, GeneralTestDoc.class, "projectb");
    GeneralTestDoc testVal = new GeneralTestDoc();
    testVal.name = "b";
    testVal.generalTestDocValue = "b";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "projectb-projectbgeneraltestdoc"));
    testVal.setCurrentVariation("projectb");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceProjectSpecificRoleWithRequestedVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    ProjectAGeneralTestDoc val = reducer.reduce(t, ProjectAGeneralTestDoc.class, "projecta");
    ProjectAGeneralTestDoc testVal = new ProjectAGeneralTestDoc();
    testVal.name = "a";
    testVal.generalTestDocValue = "a";
    testVal.projectAGeneralTestDocValue = "test";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    testVal.setCurrentVariation(null);
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  //Tests with missing variation.

  @Test
  public void testReduceCommonDataOnlyWithMissingVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class, "blah");
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceCommonDataMultipleRolesWithMissingVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}," + "\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    TestConcreteDoc val = reducer.reduce(t, TestConcreteDoc.class, "blah");
    TestConcreteDoc testVal = new TestConcreteDoc();
    testVal.name = "b";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc", "projectb-projectbgeneraltestdoc"));
    testVal.setCurrentVariation("projectb");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceRolDataAndCommonDataWithMissingVariation() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"blub\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    GeneralTestDoc val = reducer.reduce(t, GeneralTestDoc.class, "blah");
    GeneralTestDoc testVal = new GeneralTestDoc();
    testVal.name = "a";
    testVal.generalTestDocValue = "a";
    testVal.setVariations(Lists.newArrayList("testconcretedoc", "generaltestdoc", "projecta-projectageneraltestdoc"));
    testVal.setCurrentVariation("projecta");
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test(expected = VariationException.class)
  public void testReduceProjectSpecificRoleWithWrongVaration() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    JsonNode t = m.readTree(x);
    reducer.reduce(t, ProjectAGeneralTestDoc.class, "blub");
  }

  @Test(expected = VariationException.class)
  public void testReduceVariationNonObject() throws IOException {
    String x = "{\"projectb-testdoc\": \"flups\"}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestDoc.class); // This will throw
  }

  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonItem() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\": 42}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestConcreteDoc.class); // This will throw
  }

  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItem() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[42]}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestConcreteDoc.class); // This will throw
  }

  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItemAgreed() throws IOException {
    String x = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":42}]}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestConcreteDoc.class); // This will throw
  }

  @Test
  public void testGetAllForDBObjectRootClass() throws JsonProcessingException, IOException {
    registerClasses();

    String jsonString = "{\"testconcretedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    DBObject object = generateDBObject(jsonString);

    List<TestConcreteDoc> variations = reducer.getAllForDBObject(object, TestConcreteDoc.class);

    assertEquals(3, variations.size());
  }

  @Test
  public void testGetAllForDBObjectSubClass() throws JsonProcessingException, IOException {
    registerClasses();

    String jsonString = "{\"testconcretedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    DBObject object = generateDBObject(jsonString);

    List<GeneralTestDoc> variations = reducer.getAllForDBObject(object, GeneralTestDoc.class);

    assertEquals(3, variations.size());
  }

  @Test
  public void testGetAllForDBObjectRootClassWithMultipleProjects() throws JsonProcessingException, IOException {
    registerClasses();

    String jsonString = "{\"testconcretedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]},{\"v\":\"b\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"},\"projectb-projectbgeneraltestdoc\":{\"projectBGeneralTestDocValue\":\"testb\"}}";
    DBObject object = generateDBObject(jsonString);

    List<TestConcreteDoc> variations = reducer.getAllForDBObject(object, TestConcreteDoc.class);

    assertEquals(4, variations.size());
  }

  @Test
  public void testGetAllForDBObjectNonExistingClass() throws JsonProcessingException, IOException {
    registerClasses();

    String jsonString = "{\"testconcretedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"a\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projecta-projectageneraltestdoc\":{\"projectAGeneralTestDocValue\":\"test\"}}";
    DBObject object = generateDBObject(jsonString);

    List<ProjectBGeneralTestDoc> variations = reducer.getAllForDBObject(object, ProjectBGeneralTestDoc.class);

    assertEquals(3, variations.size());
  }

  private void registerClasses() {
    doReturn(TestConcreteDoc.class).when(docTypeRegistry).getClassFromMongoTypeString("testconcretedoc");
    doReturn(GeneralTestDoc.class).when(docTypeRegistry).getClassFromMongoTypeString("generaltestdoc");
    doReturn(ProjectAGeneralTestDoc.class).when(docTypeRegistry).getClassFromMongoTypeString("projecta-projectageneraltestdoc");
    doReturn(ProjectBGeneralTestDoc.class).when(docTypeRegistry).getClassFromMongoTypeString("projectb-projectbgeneraltestdoc");
  }

  private DBObject generateDBObject(String jsonString) throws JsonProcessingException, IOException {
    return new JacksonDBObject<JsonNode>(m.readTree(jsonString), JsonNode.class);
  }
}
