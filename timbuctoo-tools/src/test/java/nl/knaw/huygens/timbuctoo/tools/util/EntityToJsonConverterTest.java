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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityToJsonConverterTest {

  @org.junit.BeforeClass
  public static void setupRegistry() throws ModelException {
    // needed for type resolver
    TypeRegistry.getInstance().init("timbuctoo.model.*");
  }

  @org.junit.Test
  public void testConversion() throws IOException {
    Language language = new Language();
    language.setId("LANG00042");
    language.setRev(1);
    language.setCode("zzj");
    language.setName("Zuojiang Zhuang");
    language.setCore(false);

    String json = new EntityToJsonConverter().convert(language);

    assertThat(json, containsString("\"@type\""));
    assertThat(json, containsString("\"_id\":\"LANG00042\""));
    assertThat(json, not(containsString("\"^rev\"")));
    assertThat(json, containsString("\"^code\":\"zzj\""));
    assertThat(json, containsString("\"name\":\"Zuojiang Zhuang\""));
    assertThat(json, containsString("\"core\":false"));
  }

  @org.junit.Test
  public void testSerializationOfComplexEntity() throws Exception {
    BaseLocation location = createBaseLocation();

    String json = new EntityToJsonConverter().convert(location);
    BaseLocation converted = new ObjectMapper().readValue(json, BaseLocation.class);

    assertThat(converted.getDefLang(), equalTo("eng"));
    assertThat(converted.getUrn(), equalTo("re:derbyshire.eng"));

    Map<String, PlaceName> names = converted.getNames();
    assertThat(names, not(nullValue()));

    PlaceName name = names.get("eng");
    assertThat(name, not(nullValue()));
    assertThat(name.getDistrict(), nullValue());
    assertThat(name.getSettlement(), nullValue());
    assertThat(name.getRegion(), equalTo("Derbyshire"));
    assertThat(name.getCountry(), equalTo("England"));
    assertThat(name.getCountryCode(), equalTo("ENG"));
    assertThat(name.getBloc(), nullValue());
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
