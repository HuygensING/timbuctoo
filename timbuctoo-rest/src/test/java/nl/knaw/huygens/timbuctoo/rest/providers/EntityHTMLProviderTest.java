package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityHTMLProviderTest {

  private static final String PUBLIC_URL = "http://nl.knaw.huygens/test";

  private EntityHTMLProvider provider;

  @Before
  public void setup() {
    provider = new EntityHTMLProvider(null, "link", PUBLIC_URL);
  }

  private void assertIsWritable(boolean expected, Class<?> type, MediaType mediaType) {
    Assert.assertEquals(expected, provider.isWriteable(type, null, type.getAnnotations(), mediaType));
  }

  @Test
  public void entityIsWritable() {
    assertIsWritable(true, Entity.class, MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, Entity.class, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void subClassIsWritable() {
    assertIsWritable(true, User.class, MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, User.class, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void otherClassIsWritable() {
    assertIsWritable(false, EntityHTMLProvider.class, MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, EntityHTMLProvider.class, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testWriteTo() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    provider.writeTo(new User(), User.class, null, User.class.getAnnotations(), MediaType.TEXT_HTML_TYPE, null, out);
    String value = out.toString();
    Assert.assertTrue(value.startsWith("<!DOCTYPE html>\n<html>\n<head>\n"));
    Assert.assertTrue(value.contains("<base href=\"" + PUBLIC_URL + "/domain/\">"));
    Assert.assertTrue(value.endsWith("</body>\n</html>\n"));
  }

}
