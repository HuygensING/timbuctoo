package nl.knaw.huygens.timbuctoo.rest.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Provider
@Produces(MediaType.TEXT_HTML)
@Singleton
public class EntityHTMLProvider implements MessageBodyWriter<Entity> {

  private final HTMLProviderHelper helper;

  @Inject
  public EntityHTMLProvider(TypeRegistry registry, @Named("html.defaultstylesheet") String stylesheetLink, @Named("public_url") String publicURL) {
    helper = new HTMLProviderHelper(registry, stylesheetLink, publicURL);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return helper.accept(mediaType) && TypeRegistry.isEntity(type);
  }

  @Override
  public long getSize(Entity doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Entity doc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException,
      WebApplicationException {
    helper.writeHeader(out, doc.getDisplayName());

    JsonGenerator jgen = helper.getGenerator(out);
    ObjectWriter writer = helper.getObjectWriter(annotations);
    writer.writeValue(jgen, doc);

    helper.writeFooter(out);
  }

}
