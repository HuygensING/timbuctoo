package nl.knaw.huygens.timbuctoo.rest.util.serialization;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
    jgen.writeStringField(Change.CLIENT_PROP_USERNAME, getUsername(value));

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
