package nl.knaw.huygens.repository.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
public class DocumentHTMLProvider implements MessageBodyWriter<Document> {

  private final JsonFactory factory;
  private final Map<AnnotationBundleKey, ObjectWriter> writers;
  private final String preamble;

  @Inject
  public DocumentHTMLProvider(@Named("html.defaultstylesheet") String stylesheetLink, @Named("public_url") String publicURL) {
    factory = new JsonFactory();
    writers = Maps.newHashMap();
    preamble = getPreamble(stylesheetLink, publicURL);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return accept(mediaType) && accept(type);
  }

  private boolean accept(MediaType mediaType) {
    return MediaType.TEXT_HTML_TYPE.equals(mediaType);
  }

  private boolean accept(Class<?> type) {
    return Document.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Document doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Document doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException,
      WebApplicationException {
    write(out, preamble);
    write(out, String.format("<title>%1$s</title></head><body><h1>%1$s</h1>", getDocTitle(doc)));

    JsonGenerator jgen = new HTMLGenerator(factory.createGenerator(out));
    ObjectWriter writer = getObjectWriter(annotations);
    writer.writeValue(jgen, doc);

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
