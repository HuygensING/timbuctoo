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

import nl.knaw.huygens.timbuctoo.model.Language;

import org.junit.Assert;
import org.junit.Test;

public class EntityToJsonConverterTest {

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

    assertContains(false, json, "'@type'");
    assertContains(true, json, "'_id':'LANG00042'");
    assertContains(false, json, "'^rev'");
    assertContains(true, json, "'^code':'zzj'");
    assertContains(true, json, "'name':'Zuojiang Zhuang'");
    assertContains(true, json, "'core':false");
  }

  private void assertContains(boolean expected, String json, String text) {
    Assert.assertEquals(expected, json.contains(text.replaceAll("'", "\"")));
  }

}
