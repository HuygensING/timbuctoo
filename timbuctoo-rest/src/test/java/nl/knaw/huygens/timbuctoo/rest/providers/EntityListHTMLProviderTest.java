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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Assert;
import org.junit.Test;

public class EntityListHTMLProviderTest {

  public static class Resource {
    public Entity getDocument() {
      return null;
    }

    public List<? extends Entity> getGenericDocumentList() {
      return null;
    }

    public List<? extends User> getGenericUserList() {
      return null;
    }

    public List<Entity> getDocumentList() {
      return null;
    }

    public List<User> getUserList() {
      return null;
    }

    public List<String> getStringList() {
      return null;
    }
  }

  private void assertIsWritable(boolean expected, String methodName, MediaType mediaType) {
    EntityListHTMLProvider provider = new EntityListHTMLProvider(null, "link", "url");
    try {
      Method method = Resource.class.getMethod(methodName);
      Class<?> type = method.getReturnType();
      Type entityType = method.getGenericReturnType();
      Assert.assertEquals(expected, provider.isWriteable(type, entityType, type.getAnnotations(), mediaType));
    } catch (Exception e) {
      Assert.fail(e.getClass().getSimpleName() + " - " + e.getMessage());
    }
  }

  @Test
  public void testDocument() {
    assertIsWritable(false, "getDocument", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getDocument", MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testGenericDocumentList() {
    assertIsWritable(true, "getGenericDocumentList", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getGenericDocumentList", MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testGenericUserList() {
    assertIsWritable(true, "getGenericUserList", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getGenericUserList", MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testDocumentList() {
    assertIsWritable(true, "getDocumentList", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getDocumentList", MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testUserList() {
    assertIsWritable(true, "getUserList", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getUserList", MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testStringList() {
    assertIsWritable(false, "getStringList", MediaType.TEXT_HTML_TYPE);
    assertIsWritable(false, "getStringList", MediaType.APPLICATION_JSON_TYPE);
  }

}
