package nl.knaw.huygens.repository.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.json.annotation.EndpointConfig;
import com.fasterxml.jackson.jaxrs.json.util.AnnotationBundleKey;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Provider
@Produces(MediaType.TEXT_HTML)
@Singleton
public class DocumentListHTMLProvider implements MessageBodyWriter<List<? extends Document>> {

  private final JsonFactory factory;
  private final Map<AnnotationBundleKey, ObjectWriter> writers;
  private final String preamble;

  @Inject
  public DocumentListHTMLProvider(@Named("html.defaultstylesheet") String stylesheetLink, @Named("public_url") String publicURL) {
    factory = new JsonFactory();
    writers = Maps.newHashMap();
    preamble = getPreamble(stylesheetLink, publicURL);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return accept(mediaType) && accept(type) && accept(genericType);
  }

  private boolean accept(MediaType mediaType) {
    return MediaType.TEXT_HTML_TYPE.equals(mediaType);
  }

  private boolean accept(Class<?> type) {
    return List.class.isAssignableFrom(type);
  }

  private boolean accept(Type genericType) {
    Class<?> type = null;
    if (genericType instanceof ParameterizedType) {
      Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
      if (actualTypes.length == 1) {
        Type actualType = actualTypes[0];
        if (actualType instanceof Class<?>) {
          type = (Class<?>) actualType;
        } else if (actualType instanceof WildcardType) {
          Type[] bounds = ((WildcardType) actualType).getUpperBounds();
          if (bounds.length == 1 && bounds[0] instanceof Class<?>) {
            type = (Class<?>) bounds[0];
          }
        }
      }
    }
    return type != null && Document.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(List<? extends Document> doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(List<? extends Document> docs, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out)
      throws IOException, WebApplicationException {
    write(out, preamble);
    write(out, String.format("<title>%1$s</title></head><body><h1>%1$s</h1>", getTitle(docs)));

    JsonGenerator jgen = new HTMLGenerator(factory.createGenerator(out));
    ObjectWriter writer = getObjectWriter(annotations);
    for (Document doc : docs) {
      write(out, "<h2>");
      write(out, getDocTitle(doc));
      write(out, "</h2>");
      writer.writeValue(jgen, doc);
    }

    write(out, "</body></html>");
  }

  private void write(OutputStream out, String text) throws IOException {
    out.write(text.getBytes("UTF-8"));
  }

  private String getPreamble(String stylesheetLink, String publicURL) {
    String value = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">";
    if (!Strings.isNullOrEmpty(stylesheetLink)) {
      value += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + publicURL + stylesheetLink + "\"/>";
    }
    return value;
  }

  private String getTitle(List<? extends Document> docs) {
    String name = docs.isEmpty() ? "?" : docs.get(0).getClass().getSimpleName();
    return docs.size() + " instances of " + name;
  }

  private String getDocTitle(Document doc) {
    String description = doc.getDescription();
    return (description != null) ? StringEscapeUtils.escapeHtml(description) : "";
  }

  // FIXME make thread safe
  private ObjectWriter getObjectWriter(Annotation[] annotations) {
    AnnotationBundleKey key = new AnnotationBundleKey(annotations);
    ObjectWriter writer = writers.get(key);
    if (writer == null) {
      ObjectMapper mapper = new ObjectMapper();
      EndpointConfig endpointConfig = EndpointConfig.forWriting(mapper, annotations, null);
      writer = endpointConfig.getWriter();
      writers.put(key, writer);
    }
    return writer;
  }

}
