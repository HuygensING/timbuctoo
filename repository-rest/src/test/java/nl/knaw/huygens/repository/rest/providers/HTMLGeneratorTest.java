package nl.knaw.huygens.repository.rest.providers;

import java.io.IOException;
import java.io.StringWriter;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.rest.providers.HTMLGenerator;
import nl.knaw.huygens.repository.rest.providers.ReferenceSerializer;
import nl.knaw.huygens.repository.rest.providers.model.GeneralTestDoc;
import nl.knaw.huygens.repository.rest.providers.model.TestConcreteDoc;
import nl.knaw.huygens.repository.rest.providers.model.TestInheritsFromTestBaseDoc;
import nl.knaw.huygens.repository.rest.providers.model.TestSystemDocument;
import nl.knaw.huygens.repository.rest.providers.model.projecta.OtherDoc;
import nl.knaw.huygens.repository.rest.providers.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.rest.providers.model.projectb.ProjectBGeneralTestDoc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class HTMLGeneratorTest {

  private static final String PACKAGE = "nl.knaw.huygens.repository.rest.providers.model";
  private static final String PACKAGEA = "nl.knaw.huygens.repository.rest.providers.model.projecta";
  private static final String PACKAGEB = "nl.knaw.huygens.repository.rest.providers.model.projectb";
  private static final String MODEL_PACKAGES = PACKAGE + " " + PACKAGEA + " " + PACKAGEB;

  private HTMLGenerator gen;
  private ObjectMapper mapper;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    DocTypeRegistry registry = new DocTypeRegistry(MODEL_PACKAGES);
    SimpleModule module = new SimpleModule();
    module.addSerializer(new ReferenceSerializer(registry));
    mapper = new ObjectMapper();
    mapper.registerModule(module);
    writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator realGen = factory.createGenerator(writer);
    gen = new HTMLGenerator(realGen);
  }

  private String generateHtml(Document doc) throws JsonGenerationException, JsonMappingException, IOException {
    mapper.writeValue(gen, doc);
    return writer.getBuffer().toString();
  }

  private void assertContains(String html, String key, String value) {
    Assert.assertThat(html, Matchers.containsString("<tr><th>" + key + "</th><td>" + value + "</td></tr>"));
  }

  private void assertContains(String html, String value) {
    Assert.assertThat(html, Matchers.containsString(value.replaceAll("\\|", "\"")));
  }

  @Test
  public void testSystemDocument() throws JsonGenerationException, JsonMappingException, IOException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setAnnotatedProperty("test");
    doc.setId("TSD0000000001");
    doc.setAnnotatedProperty("anonProp");
    doc.setPropWithAnnotatedAccessors("propWithAnnotatedAccessors");

    String html = generateHtml(doc);

    assertContains(html, "Class", TestSystemDocument.class.getName());
    assertContains(html, "Name", "none");
    assertContains(html, "Test Value", "none");
    assertContains(html, "Id", "TSD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "none");
    assertContains(html, "Variations", "");
    assertContains(html, "Prop Annotated", "anonProp");
    assertContains(html, "Pwaa", "propWithAnnotatedAccessors");
    assertContains(html, "Current Variation", "none");
    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainDocumentArchetype() throws JsonGenerationException, JsonMappingException, IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId("TCD0000000001");
    doc.name = "test";
    doc.addVariation(ProjectAGeneralTestDoc.class, doc.getId());
    doc.addVariation(ProjectBGeneralTestDoc.class, doc.getId());
    doc.addVariation(GeneralTestDoc.class, doc.getId());
    doc.addVariation(TestConcreteDoc.class, doc.getId());
    doc.setCurrentVariation("projecta");
    doc.setPid("pid");

    String html = generateHtml(doc);

    assertContains(html, "Class", TestConcreteDoc.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "Id", "TCD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|projectageneraltestdocs/TCD0000000001|");
    assertContains(html, "href=|projectbgeneraltestdocs/TCD0000000001|");

    assertContains(html, "Current Variation", "projecta");
    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainDocumentSubtype() throws JsonGenerationException, JsonMappingException, IOException {
    GeneralTestDoc doc = new GeneralTestDoc();
    doc.setId("GTD0000000001");
    doc.generalTestDocValue = "generalTestDocValue";
    doc.name = "test";
    doc.addVariation(ProjectAGeneralTestDoc.class, doc.getId());
    doc.addVariation(ProjectBGeneralTestDoc.class, doc.getId());
    doc.addVariation(GeneralTestDoc.class, doc.getId());
    doc.addVariation(TestConcreteDoc.class, doc.getId());
    doc.setCurrentVariation("projecta");
    doc.setPid("pid");

    String html = generateHtml(doc);

    assertContains(html, "Class", GeneralTestDoc.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "General Test Doc Value", "generalTestDocValue");
    assertContains(html, "Id", "GTD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|projectageneraltestdocs/GTD0000000001|");
    assertContains(html, "href=|projectbgeneraltestdocs/GTD0000000001|");

    assertContains(html, "Current Variation", "projecta");
    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainDocumentProjectSubtype() throws JsonGenerationException, JsonMappingException, IOException {
    OtherDoc doc = new OtherDoc();
    doc.setId("OTD0000000001");
    doc.otherThing = "test";
    doc.setPid("pid");
    doc.addVariation(OtherDoc.class, doc.getId());
    doc.addVariation(TestInheritsFromTestBaseDoc.class, doc.getId());

    String html = generateHtml(doc);
   
    assertContains(html, "Class", OtherDoc.class.getName());
    assertContains(html, "Name", "none");
    assertContains(html, "Other Thing", "test");
    assertContains(html, "Id", "OTD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|otherdocs/OTD0000000001|");

    assertContains(html, "Current Variation", "none");
    assertContains(html, "Deleted", "no");
  }
}
