package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonNames;

import java.io.IOException;

public class PersonNamesConverter implements Converter {
  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    //convert to personNames as verification
    new ObjectMapper().treeToValue(json, PersonNames.class);
    //if this doesn't throw then it was a good personName apparently
    return json.toString();
  }

  @Override
  public JsonNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      JsonNode json = new ObjectMapper().readTree((String) value);
      //convert to personNames as verification
      new ObjectMapper().treeToValue(json, PersonNames.class);
      //if this doesn't throw then it was a good personName apparently
      return json;
    } else {
      throw new IOException("must be a json value serialised as String");
    }
  }
}
