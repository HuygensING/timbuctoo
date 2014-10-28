package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Login;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LoginCollectionDeserializer extends JsonDeserializer<LoginCollection> {

  @Override
  public LoginCollection deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    List<Login> logins = jp.readValueAs(new TypeReference<List<Login>>() {});

    return new LoginCollection(logins);
  }
}
