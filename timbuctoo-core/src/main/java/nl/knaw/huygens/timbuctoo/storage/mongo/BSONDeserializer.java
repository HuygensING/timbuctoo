package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Map;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

public class BSONDeserializer extends JsonDeserializer<BSONObject> {

  UntypedObjectDeserializer nestedSer = new UntypedObjectDeserializer();

  @Override
  public BSONObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    @SuppressWarnings("unchecked")
    Map<Object, Object> x = (Map<Object, Object>) nestedSer.deserialize(jp, ctxt);
    return new BasicBSONObject(x);
  }

}
