package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FileCollectionSerializer extends JsonSerializer<FileCollection<?>> {

  @Override
  public void serialize(FileCollection<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    // array is need to save the type information.
    provider.defaultSerializeValue(value.asArray(), jgen);
  }

}
