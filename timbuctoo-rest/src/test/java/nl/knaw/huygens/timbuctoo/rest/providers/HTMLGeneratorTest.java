package nl.knaw.huygens.timbuctoo.rest.providers;

import java.io.StringWriter;

import nl.knaw.huygens.timbuctoo.config.TypeNameGenerator;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.rest.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.rest.model.TestDomainEntity;
import nl.knaw.huygens.timbuctoo.rest.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.ProjectADomainEntity;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class HTMLGeneratorTest {

  private static final String PACKAGES = "timbuctoo.rest.model timbuctoo.rest.model.projecta timbuctoo.rest.model.projectb";

  private static TypeRegistry registry;

  private HTMLGenerator gen;
  private ObjectMapper mapper;
  private StringWriter writer;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry(PACKAGES);
  }

  @Before
  public void setup() throws Exception {
    SimpleModule module = new SimpleModule();
    module.addSerializer(new ReferenceSerializer(registry));
    mapper = new ObjectMapper();
    mapper.registerModule(module);
    writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator realGen = factory.createGenerator(writer);
    gen = new HTMLGenerator(realGen);
  }

  private String generateHtml(Entity doc) throws Exception {
    mapper.writeValue(gen, doc);
    return writer.getBuffer().toString();
  }

  private void addVariations(DomainEntity entity, Class<?>... types) {
    for (Class<?> type : types) {
      entity.addVariation(TypeNameGenerator.getInternalName(type));
    }
  }

  private void assertContains(String html, String key, String value) {
    Assert.assertThat(html, Matchers.containsString("<tr><th>" + key + "</th><td>" + value + "</td></tr>"));
  }

  private void assertContains(String html, String value) {
    Assert.assertThat(html, Matchers.containsString(value.replaceAll("\\|", "\"")));
  }

  @Test
  public void testSystemEntity() throws Exception {
    String id = "TSYS000000000001";
    TestSystemEntity entity = new TestSystemEntity(id);
    entity.setAnnotatedProperty("test");
    entity.setAnnotatedProperty("anonProp");
    entity.setPropWithAnnotatedAccessors("propWithAnnotatedAccessors");

    String html = generateHtml(entity);

    assertContains(html, "Class", TestSystemEntity.class.getName());
    assertContains(html, "Name", "none");
    assertContains(html, "Test Value", "none");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Prop Annotated", "anonProp");
    assertContains(html, "Pwaa", "propWithAnnotatedAccessors");
  }

  @Test
  public void testDomainEntityArchetype() throws Exception {
    String id = "TCD0000000001";
    TestDomainEntity entity = new TestDomainEntity(id, "test");
    entity.setPid("pid");
    addVariations(entity, TestDomainEntity.class);

    String html = generateHtml(entity);

    assertContains(html, "Class", TestDomainEntity.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|testdomainentities/TCD0000000001|");

    assertContains(html, "Deleted", "no");
  }

  @Test
  public void testDomainEntitySubtype() throws Exception {
    String id = "GTD0000000001";
    ProjectADomainEntity entity = new ProjectADomainEntity(id);
    entity.setPid("pid");
    entity.generalTestDocValue = "generalTestDocValue";
    entity.name = "test";
    addVariations(entity, BaseDomainEntity.class, ProjectADomainEntity.class);

    String html = generateHtml(entity);

    assertContains(html, "Class", ProjectADomainEntity.class.getName());
    assertContains(html, "Name", "test");
    assertContains(html, "General Test Doc Value", "generalTestDocValue");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Last Change", "none");
    assertContains(html, "Creation", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|basedomainentitys/GTD0000000001|");
    assertContains(html, "href=|projectadomainentities/GTD0000000001|");

    assertContains(html, "Deleted", "no");
  }

}
