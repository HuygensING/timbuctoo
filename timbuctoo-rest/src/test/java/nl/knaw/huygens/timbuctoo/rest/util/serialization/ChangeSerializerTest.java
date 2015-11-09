package nl.knaw.huygens.timbuctoo.rest.util.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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

import static org.hamcrest.Matchers.containsString;
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
    String generatedJson = writer.toString();

    assertThat(generatedJson, containsString(String.format("\"userId\":\"%s\"", USER_ID)));
    assertThat(generatedJson, containsString(String.format("\"vreId\":\"%s\"", VRE_ID)));
    assertThat(generatedJson, containsString(String.format("\"timeStamp\":%d", TIMESTAMP)));
    assertThat(generatedJson, containsString(String.format("\"username\":\"%s\"", USERNAME)));
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
    String generatedJson = writer.toString();
    assertThat(generatedJson, containsString(String.format("\"username\":\"%s\"", USER_ID)));
  }

  @Test
  public void serializeSetsTheUsernameWithTheIdWhenTheUserHasNoDisplayname() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);
    userWithNameFoundForId(USERNAME, null);

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    String generatedJson = writer.toString();
    assertThat(generatedJson, containsString(String.format("\"username\":\"%s\"", USER_ID)));
  }

  @Test
  public void serializeSetsTheUsernameWithTheIdWhenTheUserHasAnEmptyDisplayname() throws Exception {
    // setup
    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);
    userWithNameFoundForId(USERNAME, "");

    // action
    instance.serialize(change, generator, NULL_PROVIDER);

    // verify
    String generatedJson = writer.toString();
    assertThat(generatedJson, containsString(String.format("\"username\":\"%s\"", USER_ID)));
  }
}
