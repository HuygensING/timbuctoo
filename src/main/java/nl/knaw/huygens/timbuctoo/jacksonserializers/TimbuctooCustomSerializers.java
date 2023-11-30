package nl.knaw.huygens.timbuctoo.jacksonserializers;

import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.knaw.huygens.timbuctoo.jacksonserializers.mediatype.MediaTypeDeSerializer;
import nl.knaw.huygens.timbuctoo.jacksonserializers.mediatype.MediaTypeSerializer;

import javax.ws.rs.core.MediaType;

public class TimbuctooCustomSerializers extends SimpleModule {
  public TimbuctooCustomSerializers() {
    this.addDeserializer(MediaType.class, new MediaTypeDeSerializer());
    this.addSerializer(MediaType.class, new MediaTypeSerializer());
  }
}
