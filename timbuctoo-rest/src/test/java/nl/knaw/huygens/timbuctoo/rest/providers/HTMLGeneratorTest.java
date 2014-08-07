package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.StringWriter;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.ModelException;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.rest.model.BaseDomainEntity;
import test.rest.model.TestDomainEntity;
import test.rest.model.TestSystemEntity;
import test.rest.model.projecta.ProjectADomainEntity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Note that {@code HTMLGenerator} does not know about the proper structure
 * of entities. E.g., it does not know which variations can be present.
 */
public class HTMLGeneratorTest {

  private static TypeRegistry registry;

  @BeforeClass
  public static void setupRegistry() throws ModelException {
    registry = TypeRegistry.getInstance().init("test.rest.model.*");
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  // ---------------------------------------------------------------------------

  private HTMLGenerator htmlGenerator;
  private ObjectMapper mapper;
  private StringWriter writer;

  @Before
  public void setup() throws Exception {
    SimpleModule module = new SimpleModule();
    module.addSerializer(new ReferenceSerializer(registry));
    mapper = new ObjectMapper();
    mapper.registerModule(module);
    writer = new StringWriter();
    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer);
    htmlGenerator = new HTMLGenerator(jsonGenerator);
  }

  private String generateHtml(Entity doc) throws Exception {
    mapper.writeValue(htmlGenerator, doc);
    return writer.getBuffer().toString();
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

    assertContains(html, "Type", TypeNames.getInternalName(TestSystemEntity.class));
    assertContains(html, "Name", "none");
    assertContains(html, "Test Value", "none");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Created", "none");
    assertContains(html, "Modified", "none");
    assertContains(html, "Prop Annotated", "anonProp");
    assertContains(html, "Pwaa", "propWithAnnotatedAccessors");
  }

  @Test
  public void testDomainEntityArchetype() throws Exception {
    String id = "TCD0000000001";
    TestDomainEntity entity = new TestDomainEntity(id, "test");
    entity.setPid("pid");
    entity.addVariation(TestDomainEntity.class);

    String html = generateHtml(entity);

    assertContains(html, "Type", TypeNames.getInternalName(TestDomainEntity.class));
    assertContains(html, "Name", "test");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Created", "none");
    assertContains(html, "Modified", "none");
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
    entity.addVariation(BaseDomainEntity.class);
    entity.addVariation(ProjectADomainEntity.class);

    String html = generateHtml(entity);

    assertContains(html, "Type", TypeNames.getInternalName(ProjectADomainEntity.class));
    assertContains(html, "Name", "test");
    assertContains(html, "General Test Doc Value", "generalTestDocValue");
    assertContains(html, "Id", id);
    assertContains(html, "Rev", "0");
    assertContains(html, "Created", "none");
    assertContains(html, "Modified", "none");
    assertContains(html, "Pid", "pid");

    assertContains(html, "href=|basedomainentities/GTD0000000001|");
    assertContains(html, "href=|projectadomainentities/GTD0000000001|");

    assertContains(html, "Deleted", "no");
  }

}
