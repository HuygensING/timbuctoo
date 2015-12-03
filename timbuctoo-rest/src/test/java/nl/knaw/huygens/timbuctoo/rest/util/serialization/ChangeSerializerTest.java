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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.util.Change.CLIENT_PROP_TIME_STAMP;
import static nl.knaw.huygens.timbuctoo.model.util.Change.CLIENT_PROP_USERNAME;
import static nl.knaw.huygens.timbuctoo.model.util.Change.CLIENT_PROP_USER_ID;
import static nl.knaw.huygens.timbuctoo.model.util.Change.CLIENT_PROP_VRE_ID;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeSerializerTest {

  private static final SerializerProvider NULL_PROVIDER = null;
  public static final String USER_ID = "userId";
  public static final String VRE_ID = "vreId";
  public static final long TIMESTAMP = new Date().getTime();
  public static final String USERNAME = "username";
  private Change change;
  private ChangeSerializer instance;
  private UserConfigurationHandler users;

  @Before
  public void setUp() throws Exception {
    change = new Change(TIMESTAMP, USER_ID, VRE_ID);
    users = mock(UserConfigurationHandler.class);
    instance = new ChangeSerializer(users);
  }

  @Test
  public void serializeAddsAFieldUsernameWithTheUsernameCorrespondingWithTheUserId() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);
    userWithNameFoundForId(USERNAME, USER_ID);

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    Map<String, Object> valueAsMap = valueAsMap(writer);

    assertThat(valueAsMap.keySet(), containsInAnyOrder(CLIENT_PROP_TIME_STAMP, CLIENT_PROP_USER_ID, CLIENT_PROP_USERNAME, CLIENT_PROP_VRE_ID));
    assertThat(valueAsMap.get(CLIENT_PROP_USER_ID), is(USER_ID));
    assertThat(valueAsMap.get(CLIENT_PROP_VRE_ID), is(VRE_ID));
    assertThat(valueAsMap.get(CLIENT_PROP_TIME_STAMP), is(TIMESTAMP));
    assertThat(valueAsMap.get(CLIENT_PROP_USERNAME), is(USERNAME));
  }

  private Map<String, Object> valueAsMap(StringWriter writer) throws IOException {
    return new ObjectMapper().readValue(writer.toString(), new TypeReference<Map<String, Object>>() {
    });
  }

  private void userWithNameFoundForId(String username, String userId) {
    User user = new User();
    user.setDisplayName(username);
    when(users.getUser(userId)).thenReturn(user);
  }

  private JsonGenerator createGenerator(StringWriter writer) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonFactory factory = objectMapper.getFactory();
    return factory.createGenerator(writer);
  }

  @Test
  public void serializeSetsTheUsernameWithTheIdWhenTheUserCannotBeFound() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    Map<String, Object> valueAsMap = valueAsMap(writer);
    assertThat(valueAsMap.get(CLIENT_PROP_USERNAME), is(USER_ID));
  }

  @Test
  public void serializeSetsTheUsernameWithTheIdWhenTheUserHasNoDisplayname() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);
    userWithNameFoundForId(null, USER_ID);

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    Map<String, Object> valueAsMap = valueAsMap(writer);
    assertThat(valueAsMap.get(CLIENT_PROP_USERNAME), is(USER_ID));
  }

  @Test
  public void serializeSetsTheUsernameWithTheIdWhenTheUserHasAnEmptyDisplayname() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);
    userWithNameFoundForId("", USER_ID);

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    Map<String, Object> valueAsMap = valueAsMap(writer);
    assertThat(valueAsMap.get(CLIENT_PROP_USERNAME), is(USER_ID));
  }
}
