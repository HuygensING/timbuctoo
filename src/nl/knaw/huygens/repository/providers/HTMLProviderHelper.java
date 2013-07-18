package nl.knaw.huygens.repository.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.config.DocTypeRegistry;

import org.apache.commons.lang.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.annotation.EndpointConfig;
import com.fasterxml.jackson.jaxrs.json.util.AnnotationBundleKey;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Contains functionality common to the various HTML providers.
 */
public class HTMLProviderHelper {

  private final DocTypeRegistry registry;
  private final Map<AnnotationBundleKey, ObjectWriter> writers;
  private final JsonFactory factory;
  private final String preamble;

  public HTMLProviderHelper(DocTypeRegistry registry, String stylesheetLink, String publicURL) {
    this.registry = registry;
    writers = Maps.newHashMap();
    factory = new JsonFactory();
    preamble = createPreamble(stylesheetLink, publicURL);
  }

  /**
   * Returns <code>true</code> for accepted media types, <code>false</code> otherwise.
   */
  public boolean accept(MediaType mediaType) {
    return MediaType.TEXT_HTML_TYPE.equals(mediaType);
  }

  private String createPreamble(String stylesheetLink, String publicURL) {
    String value = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">";
    if (!Strings.isNullOrEmpty(stylesheetLink)) {
      value += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + publicURL + stylesheetLink + "\"/>";
    }
    //Makes it easier to redirect the links of the references.
    value += "<base href=\"" + publicURL + "/resources/\">";
    return value;
  }

  /**
   * Writes the specfied text to the output stream.
   */
  public void write(OutputStream out, String text) throws IOException {
    out.write(text.getBytes("UTF-8"));
  }

  /**
   * Writes the header to the output stream.
   */
  public void writeHeader(OutputStream out, String title) throws IOException {
    write(out, preamble);
    String text = (title != null) ? StringEscapeUtils.escapeHtml(title) : "";
    write(out, String.format("<title>%1$s</title></head><body><h1>%1$s</h1>", text));
  }

  /**
   * Writes the footer to the output stream.
   */
  public void writeFooter(OutputStream out) throws IOException {
    write(out, "</body></html>");
  }

  /**
   * Returns a generator for the specified output stream. The HTML generator
   * is implemented as a subclass of JsonGenerator.
   */
  public JsonGenerator getGenerator(OutputStream out) throws IOException {
    return new HTMLGenerator(factory.createGenerator(out));
  }

  /**
   * Returns an object writer for a class with the specified annotations.
   * Note that the current implementation is not thread safe.
   */
  public ObjectWriter getObjectWriter(Annotation[] annotations) {
    AnnotationBundleKey key = new AnnotationBundleKey(annotations);
    ObjectWriter writer = writers.get(key);
    if (writer == null) {
      //A quick hack to add custom serialization of the Reference type.
      SimpleModule module = new SimpleModule();
      module.addSerializer(new ReferenceSerializer(registry));
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(module);
      EndpointConfig endpointConfig = EndpointConfig.forWriting(mapper, annotations, null);
      writer = endpointConfig.getWriter();
      writers.put(key, writer);
    }
    return writer;
  }

}
