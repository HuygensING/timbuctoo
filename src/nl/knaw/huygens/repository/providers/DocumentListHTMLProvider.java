package nl.knaw.huygens.repository.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringEscapeUtils;

import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import nl.knaw.huygens.repository.model.Document;


@Provider
@Produces(MediaType.TEXT_HTML)
@Singleton
public class DocumentListHTMLProvider implements MessageBodyWriter<List<? extends Document>> {
  private static byte[] PREAMBLE;
  private ObjectMapper mapper = new ObjectMapper();
  private JsonFactory factory = new JsonFactory();
  
  @Inject
  public DocumentListHTMLProvider(@Named("html.defaultstylesheet") String stylesheetLink, @Named("public_url") String publicURL) {
    try {
      String preambleString = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">";
      if (!Strings.isNullOrEmpty(stylesheetLink)) {
        preambleString += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + publicURL + stylesheetLink + "\"/>";
      }
      PREAMBLE = preambleString.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    if (!mediaType.toString().startsWith(MediaType.TEXT_HTML)) {
      return false;
    }
    
    boolean isWritable;
    if (List.class.isAssignableFrom(type)
        && genericType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) genericType;
      Type[] actualTypeArgs = (parameterizedType.getActualTypeArguments());
      isWritable = actualTypeArgs.length == 1;
      if (isWritable) {
        Class<?> cls = null;
        if (actualTypeArgs[0] instanceof WildcardTypeImpl) {
          WildcardTypeImpl wildcard = (WildcardTypeImpl) actualTypeArgs[0];
          Type[] bounds = wildcard.getUpperBounds();
          if (bounds.length == 1 && bounds[0] instanceof Class<?>) {
            cls = (Class<?>) bounds[0];
          }
        } else {
          cls = actualTypeArgs[0].getClass();
        }
        isWritable = cls != null && Document.class.isAssignableFrom(cls);
      }
    } else {
      isWritable = false;
    }
    
    return isWritable;
  }

  @Override
  public long getSize(List<? extends Document> t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(List<? extends Document> docs, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    entityStream.write(PREAMBLE);
    entityStream.write("<title>".getBytes("UTF-8"));
    byte[] title = encodeTitle(docs.isEmpty() ? null : docs.get(0), docs.size());
    entityStream.write(title);
    entityStream.write("</title></head><body><h1>".getBytes("UTF-8"));
    entityStream.write(title);
    entityStream.write("</h1>".getBytes("UTF-8"));
    JsonGenerator jgen = new HTMLGenerator(factory.createGenerator(entityStream));
    
    for (Document doc : docs) {
      entityStream.write("<h2>".getBytes("UTF-8"));
      entityStream.write(encodeDocTitle(doc));
      entityStream.write("</h2>".getBytes("UTF-8"));
      mapper.writeValue(jgen, doc);
    }
    entityStream.write("</body></html>".getBytes("UTF-8"));
  }

  private byte[] encodeDocTitle(Document doc) throws UnsupportedEncodingException {
   String t = StringEscapeUtils.escapeHtml(doc.getDescription() != null ? doc.getDescription() : "");
   return t.getBytes("UTF-8");
  }

  private byte[] encodeTitle(Document exampleDoc, int size) throws UnsupportedEncodingException {
    String typeInfo = exampleDoc != null ? exampleDoc.getClass().getSimpleName() : "?";
    String t = size + " instances of " + typeInfo;
    return t.getBytes("UTF-8");
  }
}
