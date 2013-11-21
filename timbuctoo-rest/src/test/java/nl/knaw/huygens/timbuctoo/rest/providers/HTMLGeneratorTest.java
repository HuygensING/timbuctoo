package nl.knaw.huygens.timbuctoo.rest.providers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.rest.providers.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestInheritsFromTestBaseDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.TestSystemDocument;
import nl.knaw.huygens.timbuctoo.rest.providers.model.projecta.OtherDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.rest.providers.model.projectb.ProjectBGeneralTestDoc;

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
import com.google.common.collect.Lists;

public class HTMLGeneratorTest {

  private static final String PACKAGE = "nl.knaw.huygens.timbuctoo.rest.providers.model";
  private static final String PACKAGEA = "nl.knaw.huygens.timbuctoo.rest.providers.model.projecta";
  private static final String PACKAGEB = "nl.knaw.huygens.timbuctoo.rest.providers.model.projectb";
  private static final String MODEL_PACKAGES = PACKAGE + " " + PACKAGEA + " " + PACKAGEB;

  private HTMLGenerator gen;
  private ObjectMapper mapper;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    TypeRegistry registry = new TypeRegistry(MODEL_PACKAGES);
    SimpleModule module = new SimpleModule();
    module.addSerializer(new ReferenceSerializer(registry));
    mapper = new ObjectMapper();
    mapper.registerModule(module);
    writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator realGen = factory.createGenerator(writer);
    gen = new HTMLGenerator(realGen);
  }

  private String generateHtml(Entity doc) throws JsonGenerationException, JsonMappingException, IOException {
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
    assertContains(html, "Prop Annotated", "anonProp");
    assertContains(html, "Pwaa", "propWithAnnotatedAccessors");
  }

  @Test
  public void testDomainDocumentArchetype() throws JsonGenerationException, JsonMappingException, IOException {
    String id = "TCD0000000001";
    TestConcreteDoc entity = new TestConcreteDoc(id, "test");
    entity.setPid("pid");
    List<Reference> variations = Lists.newArrayList();
    variations.add(new Reference(ProjectAGeneralTestDoc.class, id));
    variations.add(new Reference(ProjectBGeneralTestDoc.class, id));
    variations.add(new Reference(GeneralTestDoc.class, id));
    variations.add(new Reference(TestConcreteDoc.class, id));
    entity.setVariationRefs(variations);

    String html = generateHtml(entity);

    assertContains(html, "Class", TestConcreteDoc.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "Id", "TCD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|projectageneraltestdocs/TCD0000000001|");
    assertContains(html, "href=|projectbgeneraltestdocs/TCD0000000001|");

    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainDocumentSubtype() throws JsonGenerationException, JsonMappingException, IOException {
    String id = "GTD0000000001";
    GeneralTestDoc entity = new GeneralTestDoc(id);
    entity.setPid("pid");
    entity.generalTestDocValue = "generalTestDocValue";
    entity.name = "test";
    List<Reference> variations = Lists.newArrayList();
    variations.add(new Reference(ProjectAGeneralTestDoc.class, id));
    variations.add(new Reference(ProjectBGeneralTestDoc.class, id));
    variations.add(new Reference(GeneralTestDoc.class, id));
    variations.add(new Reference(TestConcreteDoc.class, id));
    entity.setVariationRefs(variations);

    String html = generateHtml(entity);

    assertContains(html, "Class", GeneralTestDoc.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "General Test Doc Value", "generalTestDocValue");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|projectageneraltestdocs/GTD0000000001|");
    assertContains(html, "href=|projectbgeneraltestdocs/GTD0000000001|");

    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainDocumentProjectSubtype() throws JsonGenerationException, JsonMappingException, IOException {
    String id = "OTD0000000001";
    OtherDoc entity = new OtherDoc(id);
    entity.setPid("pid");
    entity.otherThing = "test";
    List<Reference> variations = Lists.newArrayList();
    variations.add(new Reference(OtherDoc.class, id));
    variations.add(new Reference(TestInheritsFromTestBaseDoc.class, id));
    entity.setVariationRefs(variations);

    String html = generateHtml(entity);

    assertContains(html, "Class", OtherDoc.class.getName());
    assertContains(html, "Other Thing", "test");
    assertContains(html, "Id", "OTD0000000001");
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|otherdocs/OTD0000000001|");

    assertContains(html, "Deleted", "no");
  }

}
