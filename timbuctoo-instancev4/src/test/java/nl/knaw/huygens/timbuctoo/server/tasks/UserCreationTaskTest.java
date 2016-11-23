package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.security.LocalUserCreator;
import nl.knaw.huygens.timbuctoo.security.UserCreationException;
import nl.knaw.huygens.timbuctoo.server.security.UserInfoKeys;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.PrintWriter;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class UserCreationTaskTest {
  public static final String UNKNOWN_KEY_1 = "unknownKey1";
  public static final String UNKNOWN_KEY_2 = "unknownKey2";
  private static final String PID = "pid";
  private static final String USER_NAME = "user_name";
  private static final String PWD = "pwd";
  private static final String GIVEN_NAME = "Given";
  private static final String SURNAME = "Sur";
  private static final String EMAIL = "test@example.com";
  private static final String ORGANIZATION = "Org";
  private static final String VRE_ID = "VRE";
  private static final String VRE_ROLE = "role";
  private LocalUserCreator localUserCreator;
  private PrintWriter printWriter;
  private UserCreationTask instance;

  @Before
  public void setUp() throws Exception {
    localUserCreator = mock(LocalUserCreator.class);
    printWriter = mock(PrintWriter.class);
    instance = new UserCreationTask(localUserCreator);
  }

  @Test
  public void executeLetsTheLocalUserCreatorCreateAUser() throws Exception {
    Map<String, String> userInfo = Maps.newHashMap();
    userInfo.put(UserInfoKeys.USER_PID, PID);
    userInfo.put(UserInfoKeys.USER_NAME, USER_NAME);
    userInfo.put(UserInfoKeys.PASSWORD, PWD);
    userInfo.put(UserInfoKeys.GIVEN_NAME, GIVEN_NAME);
    userInfo.put(UserInfoKeys.SURNAME, SURNAME);
    userInfo.put(UserInfoKeys.EMAIL_ADDRESS, EMAIL);
    userInfo.put(UserInfoKeys.ORGANIZATION, ORGANIZATION);
    userInfo.put(UserInfoKeys.VRE_ID, VRE_ID);
    userInfo.put(UserInfoKeys.VRE_ROLE, VRE_ROLE);
    ImmutableMultimap<String, String> immutableMultimap = ImmutableMultimap.copyOf(userInfo.entrySet());

    instance.execute(immutableMultimap, mock(PrintWriter.class));

    verify(localUserCreator).create(userInfo);
  }

  @Test
  public void executeFiltersAllTheUnknownKeys() throws Exception {
    ImmutableMultimap<String, String> immutableMultimap = ImmutableMultimap.<String, String>builder()
      .put(UserInfoKeys.USER_PID, PID)
      .put(UserInfoKeys.USER_NAME, USER_NAME)
      .put(UserInfoKeys.PASSWORD, PWD)
      .put(UserInfoKeys.GIVEN_NAME, GIVEN_NAME)
      .put(UserInfoKeys.SURNAME, SURNAME)
      .put(UserInfoKeys.EMAIL_ADDRESS, EMAIL)
      .put(UserInfoKeys.ORGANIZATION, ORGANIZATION)
      .put(UserInfoKeys.VRE_ID, VRE_ID)
      .put(UserInfoKeys.VRE_ROLE, VRE_ROLE)
      .put(UNKNOWN_KEY_1, "val")
      .put(UNKNOWN_KEY_2, "val2")
      .build();

    instance.execute(immutableMultimap, mock(PrintWriter.class));

    // check the set does not has the unknown keys
    verify(localUserCreator).create(argThat(not(allOf(hasKey(UNKNOWN_KEY_1), hasKey(UNKNOWN_KEY_2)))));
  }

  @Test
  public void executeDoesNotImportWithDuplicateKeys() throws Exception {
    ImmutableMultimap<String, String> immutableMultimap = ImmutableMultimap.<String, String>builder()
      .put(UserInfoKeys.USER_PID, PID)
      .put(UserInfoKeys.USER_PID, "otherPid")
      .put(UserInfoKeys.USER_NAME, USER_NAME)
      .put(UserInfoKeys.PASSWORD, PWD)
      .put(UserInfoKeys.GIVEN_NAME, GIVEN_NAME)
      .put(UserInfoKeys.SURNAME, SURNAME)
      .put(UserInfoKeys.EMAIL_ADDRESS, EMAIL)
      .put(UserInfoKeys.ORGANIZATION, ORGANIZATION)
      .put(UserInfoKeys.VRE_ID, VRE_ID)
      .put(UserInfoKeys.VRE_ROLE, VRE_ROLE)
      .build();

    instance.execute(immutableMultimap, printWriter);

    verifyZeroInteractions(localUserCreator);
    verify(printWriter).write(argThat(containsString(UserInfoKeys.USER_PID)));
  }

  @Test
  public void executeDoesNotImportIfKeysAreMissing() throws Exception {
    // Missing USER_PID key
    ImmutableMultimap<String, String> immutableMultimap = ImmutableMultimap.<String, String>builder()
      .put(UserInfoKeys.USER_NAME, USER_NAME)
      .put(UserInfoKeys.PASSWORD, PWD)
      .put(UserInfoKeys.GIVEN_NAME, GIVEN_NAME)
      .put(UserInfoKeys.SURNAME, SURNAME)
      .put(UserInfoKeys.EMAIL_ADDRESS, EMAIL)
      .put(UserInfoKeys.ORGANIZATION, ORGANIZATION)
      .put(UserInfoKeys.VRE_ID, VRE_ID)
      .put(UserInfoKeys.VRE_ROLE, VRE_ROLE)
      .build();

    instance.execute(immutableMultimap, printWriter);

    verifyZeroInteractions(localUserCreator);
    verify(printWriter).write(argThat(containsString(UserInfoKeys.USER_PID)));
  }

  @Test(expected = UserCreationException.class)
  public void executeRethrowsTheExceptionsOfTheLocalUserCreator() throws Exception {
    doThrow(new UserCreationException("")).when(localUserCreator).create(Matchers.any());
    ImmutableMultimap<String, String> immutableMultimap = ImmutableMultimap.<String, String>builder()
      .put(UserInfoKeys.USER_PID, PID)
      .put(UserInfoKeys.USER_NAME, USER_NAME)
      .put(UserInfoKeys.PASSWORD, PWD)
      .put(UserInfoKeys.GIVEN_NAME, GIVEN_NAME)
      .put(UserInfoKeys.SURNAME, SURNAME)
      .put(UserInfoKeys.EMAIL_ADDRESS, EMAIL)
      .put(UserInfoKeys.ORGANIZATION, ORGANIZATION)
      .put(UserInfoKeys.VRE_ID, VRE_ID)
      .put(UserInfoKeys.VRE_ROLE, VRE_ROLE)
      .build();

    instance.execute(immutableMultimap, mock(PrintWriter.class));
  }

}
