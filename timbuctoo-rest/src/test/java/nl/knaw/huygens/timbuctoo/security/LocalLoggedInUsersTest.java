package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Before;
import org.junit.Test;

public class LocalLoggedInUsersTest {
  private static final String PERSISTENT_ID = "persistentId";
  private LocalLoggedInUsers instance;

  @Before
  public void setUp() {
    instance = new LocalLoggedInUsers();
  }

  @Test
  public void addMakesThePidOfTheUserRetrievableAndReturnsTheKeyToRetrieveIt() {
    // action
    String sessionKey = instance.add(PERSISTENT_ID);

    // verify
    String retrievedPID = instance.get(sessionKey);
    assertThat(retrievedPID, is(equalTo(PERSISTENT_ID)));
  }

  @Test
  public void addReturnsAKeyThatStartsWithTheLocalSessionIdPrefix() {
    // action
    String sessionKey = instance.add(PERSISTENT_ID);

    // verify
    assertThat(sessionKey, startsWith(LOCAL_SESSION_KEY_PREFIX));
  }
}
