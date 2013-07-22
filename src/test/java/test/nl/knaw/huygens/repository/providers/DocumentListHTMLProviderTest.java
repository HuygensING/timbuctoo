package nl.knaw.huygens.repository.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.User;

import org.junit.Assert;
import org.junit.Test;

public class DocumentListHTMLProviderTest {

  public static class Resource {
    public Document getDocument() {
      return null;
    }

    public List<? extends Document> getGenericDocumentList() {
      return null;
    }

    public List<? extends User> getGenericUserList() {
      return null;
    }

    public List<Document> getDocumentList() {
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
    DocumentListHTMLProvider provider = new DocumentListHTMLProvider(null, "link", "url");
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
