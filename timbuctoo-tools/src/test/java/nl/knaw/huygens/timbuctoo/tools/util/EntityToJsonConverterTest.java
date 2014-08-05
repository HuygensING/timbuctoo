package nl.knaw.huygens.timbuctoo.tools.util;

/*
 * #%L
 * Timbuctoo tools
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

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityToJsonConverterTest {

  @BeforeClass
  public static void setupRegistry() throws ModelException {
    // needed for type resolver
    TypeRegistry.getInstance().init("timbuctoo.model.*");
  }

  @Test
  public void testConversion() throws IOException {
    Language language = new Language();
    language.setId("LANG00042");
    language.setRev(1);
    language.setCode("zzj");
    language.setName("Zuojiang Zhuang");
    language.setCore(false);

    EntityToJsonConverter converter = new EntityToJsonConverter();
    String json = converter.convert(language);

    assertContains(true, json, "'@type'");
    assertContains(true, json, "'_id':'LANG00042'");
    assertContains(false, json, "'^rev'");
    assertContains(true, json, "'^code':'zzj'");
    assertContains(true, json, "'name':'Zuojiang Zhuang'");
    assertContains(true, json, "'core':false");
  }

  private void assertContains(boolean expected, String json, String text) {
    Assert.assertEquals(expected, json.contains(text.replaceAll("'", "\"")));
  }

  @Test
  public void testSerializationOfComplexEntity() throws Exception {
    EntityToJsonConverter converter = new EntityToJsonConverter();
    ObjectMapper mapper = new ObjectMapper();

    BaseLocation location = createBaseLocation();
    String json = converter.convert(location);

    BaseLocation converted = mapper.readValue(json, BaseLocation.class);
    Assert.assertEquals("eng", converted.getDefLang());
    Assert.assertEquals("re:derbyshire.eng", converted.getUrn());
    Map<String, PlaceName> names = converted.getNames();
    Assert.assertNotNull(names);
    PlaceName name = names.get("eng");
    Assert.assertNotNull(name);
    Assert.assertNull(name.getDistrict());
    Assert.assertNull(name.getSettlement());
    Assert.assertEquals("Derbyshire", name.getRegion());
    Assert.assertEquals("England", name.getCountry());
    Assert.assertEquals("ENG", name.getCountryCode());
    Assert.assertNull(name.getBloc());
  }

  private BaseLocation createBaseLocation() {
    PlaceName name = new PlaceName();
    name.setRegion("Derbyshire");
    name.setCountry("England");
    name.setCountryCode("ENG");
    BaseLocation location = new BaseLocation();
    location.setDefLang("eng");
    location.setUrn("re:derbyshire.eng");
    location.addName("eng", name);
    return location;
  }

}
