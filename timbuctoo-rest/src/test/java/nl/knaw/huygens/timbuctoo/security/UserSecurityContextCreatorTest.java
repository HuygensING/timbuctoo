package nl.knaw.huygens.timbuctoo.security;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;

import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class UserSecurityContextCreatorTest {

  private static final String USER_ID = "test123";
  private static final String DISPLAY_NAME = "displayName";
  private UserSecurityContextCreator instance;
  private StorageManager storageManager;

  @Before
  public void setUp() {
    storageManager = mock(StorageManager.class);
    instance = new UserSecurityContextCreator(storageManager);
  }

  @After
  public void tearDown() {
    reset(storageManager);
  }

  @Test
  public void testCreateSecurityContextKnownUser() throws IOException {
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);

    when(storageManager.findEntity(User.class, example)).thenReturn(user);

    instance.createSecurityContext(securityInformation);

    verify(storageManager, only()).findEntity(User.class, example);
    verify(storageManager, never()).addEntity(Matchers.<Class<User>> any(), any(User.class));
  }

  protected User createUser(String displayName, String userId) {
    User user = new User();
    user.setDisplayName(displayName);
    user.setPersistentId(userId);
    return user;
  }

  protected SecurityInformation createSecurityInformation(String displayName, String userId) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(userId);

    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setDisplayName(displayName);
    securityInformation.setPrincipal(principal);
    return securityInformation;
  }

  @Test
  public void testCreateSecurityContextUnknownUser() throws IOException {
    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    when(storageManager.findEntity(Matchers.<Class<User>> any(), any(User.class))).thenReturn(null, user);

    instance.createSecurityContext(securityInformation);

    verify(storageManager, times(2)).findEntity(Matchers.<Class<User>> any(), any(User.class));
    verify(storageManager, times(1)).addEntity(Matchers.<Class<User>> any(), any(User.class));
  }

  @Test
  public void testCreateSecurityContextParamNull() {
    assertNull(instance.createSecurityContext(null));
  }
}
