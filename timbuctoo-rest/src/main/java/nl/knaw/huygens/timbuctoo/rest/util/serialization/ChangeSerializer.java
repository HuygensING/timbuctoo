package nl.knaw.huygens.timbuctoo.rest.util.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;

import java.io.IOException;

public class ChangeSerializer extends JsonSerializer<Change> {
  private final UserConfigurationHandler users;

  public ChangeSerializer(UserConfigurationHandler users) {
    this.users = users;
  }

  @Override
  public void serialize(Change value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartObject();

    jgen.writeStringField(Change.CLIENT_PROP_USER_ID, value.getUserId());
    jgen.writeStringField(Change.CLIENT_PROP_VRE_ID, value.getVreId());
    jgen.writeNumberField(Change.CLIENT_PROP_TIME_STAMP, value.getTimeStamp());
    jgen.writeStringField("username", getUsername(value));

    jgen.writeEndObject();
    jgen.flush(); // needed to write the data to the underlying java.io.Writer

  }

  private String getUsername(Change value) {
    String userId = value.getUserId();
    User user = users.getUser(userId);
    if(user == null){
      return userId;
    }
    String displayName = user.getDisplayName();
    return Strings.isNullOrEmpty(displayName) ? userId : displayName;
  }
}
