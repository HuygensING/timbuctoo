package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class VariationReducerTest {
  private VariationReducer reducer;
  private ObjectMapper m;

  @Before
  public void setUp() {
    m = new ObjectMapper();
    reducer = new VariationReducer(new DocumentTypeRegister());
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
    String x = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"blub\"]}],\"!defaultVRE\":\"blub\"}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    testVal.setVariations(Lists.newArrayList("testbasedoc"));
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
    String x = "{\"testbasedoc\":{\"name\": 42}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestDoc.class); // This will throw
  }

  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItem() throws IOException {
    String x = "{\"testbasedoc\":{\"name\":[42]}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestDoc.class); // This will throw
  }

  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItemAgreed() throws IOException {
    String x = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":42}]}}";
    JsonNode t = m.readTree(x);

    reducer.reduce(t, TestDoc.class); // This will throw
  }
}
