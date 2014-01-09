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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;

import org.apache.commons.lang.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.cfg.AnnotationBundleKey;
import com.fasterxml.jackson.jaxrs.json.JsonEndpointConfig;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Contains functionality common to the various HTML providers.
 */
public class HTMLProviderHelper {

  private final TypeRegistry registry;
  private final Map<AnnotationBundleKey, ObjectWriter> writers;
  private final JsonFactory factory;
  private final String preamble;

  public HTMLProviderHelper(TypeRegistry registry, String stylesheetLink, String publicURL) {
    this.registry = registry;
    writers = Maps.newHashMap();
    factory = new JsonFactory();
    preamble = createPreamble(publicURL, stylesheetLink);
  }

  /**
   * Returns <code>true</code> for accepted media types, <code>false</code> otherwise.
   */
  public boolean accept(MediaType mediaType) {
    return MediaType.TEXT_HTML_TYPE.equals(mediaType);
  }

  private String createPreamble(String publicURL, String stylesheet) {
    StringBuilder builder = new StringBuilder();
    builder.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=|UTF-8|>\n");
    if (!Strings.isNullOrEmpty(stylesheet)) {
      builder.append("<link rel=|stylesheet| type=|text/css| href=|").append(publicURL).append(stylesheet).append("|/>\n");
    }
    // Make it easier to redirect the links of the references.
    builder.append("<base href=|").append(publicURL).append('/').append(Paths.DOMAIN_PREFIX).append("/|>\n");
    return CharMatcher.is('|').replaceFrom(builder, '"');
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
    write(out, String.format("<title>%1$s</title>\n</head>\n<body>\n<h1>%1$s</h1>", text));
  }

  /**
   * Writes the footer to the output stream.
   */
  public void writeFooter(OutputStream out) throws IOException {
    write(out, "</body>\n</html>\n");
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
   * <ul>
   * <li>The current implementation is not thread safe.</li>
   * <li>Apparently uses undocumented features of Jackson. In version 2.1 it used
   * <code>com.fasterxml.jackson.jaxrs.json.util.AnnotationBundleKey</code> and
   * <code>com.fasterxml.jackson.jaxrs.json.annotation.EndpointConfig</code>.
   * In Jackson 2.2 the first of these classes was moved to a different package,
   * and the second was replaced by <code>JsonEndpointConfig</code>.</li>
   * </ul>
   */
  public ObjectWriter getObjectWriter(Annotation[] annotations) {
    AnnotationBundleKey key = new AnnotationBundleKey(annotations, AnnotationBundleKey.class);
    ObjectWriter writer = writers.get(key);
    if (writer == null) {
      // A quick hack to add custom serialization of the Reference type.
      SimpleModule module = new SimpleModule();
      module.addSerializer(new ReferenceSerializer(registry));
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(module);
      JsonEndpointConfig endpointConfig = JsonEndpointConfig.forWriting(mapper, annotations, null);
      writer = endpointConfig.getWriter();
      writers.put(key, writer);
    }
    return writer;
  }

}
