package nl.knaw.huygens.timbuctoo.server.rest.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.search.FacetValue;

import java.io.IOException;

public class FacetValueDeserializer extends JsonDeserializer<FacetValue> {
  @Override
  public FacetValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
    throws IOException, JsonProcessingException {
    JsonNode facetValue = jsonParser.readValueAsTree();

    if (facetValue.has("values")) {
      return jsonParser.readValueAs(ListFacetValue.class);
    }
    else {
      return jsonParser.readValueAs(DateRangeFacetValue.class);
    }
  }
}
