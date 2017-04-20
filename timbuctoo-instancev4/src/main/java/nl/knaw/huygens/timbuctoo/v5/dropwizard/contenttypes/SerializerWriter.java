package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializationFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class SerializerWriter implements MessageBodyWriter<SerializableObject> {
  private final SerializationFactory serializationFactory;

  protected SerializerWriter(SerializationFactory serializationFactory) {
    this.serializationFactory = serializationFactory;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Serializable.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(SerializableObject serializable, Class<?> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType) {
    return 0;
  }

  @Override
  public void writeTo(SerializableObject serializable, Class<?> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    serializable.performSerialization(serializationFactory.create(entityStream));
  }
}
