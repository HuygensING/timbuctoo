package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Before;
import org.junit.Test;

public class DefaultVREAuthorizationHandlerTest {
  private static final String USER_ID = "userId";
  private static final String VRE_ID = "vreId";
  private static final User USER;
  static {
    USER = new User();
    USER.setId(USER_ID);
  }

  private UserConfigurationHandler userConfigurationHandlerMock;
  private MailSender mailSenderMock;
  private DefaultVREAuthorizationHandler instance;

  @Before
  public void setUp() {
    userConfigurationHandlerMock = mock(UserConfigurationHandler.class);
    mailSenderMock = mock(MailSender.class);
    instance = new DefaultVREAuthorizationHandler(userConfigurationHandlerMock, mailSenderMock);
  }

  @Test
  public void whenTheAuthorizationExistsItShouldBeReturned() {
    // setup
    findVREAuthorizationFor(VRE_ID, USER_ID);

    // action
    instance.getVREAuthorization(VRE_ID, USER);

    // verify
    verifyVREAuthorizationIsSearchedFor(VRE_ID, USER_ID);
  }

  @Test
  public void whenTheAuthorizationDoesNotExistANewOneWithTheRoleUnverifiedShouldBeCreatedAndAnEmailShouldBeSendToTheAdminOfTheVRE() throws StorageException, ValidationException {
    // setup
    findAnAdminOfVRE(VRE_ID);

    // action
    instance.getVREAuthorization(VRE_ID, USER);

    // verify
    verifyVREAuthorizationIsSearchedFor(VRE_ID, USER_ID);
    verifyAVREAuthorizationIsCreatedFor(VRE_ID, USER_ID, UNVERIFIED_USER_ROLE);
    verifyAnEmailIsSendToAnAdminOfTheVRE();
  }

  @Test
  public void whenTheAuthorizationDoesNotExistANewOneWithTheRoleUnverifiedShouldBeCreatedAndNoEmailIsSendWhenTheVREHasNoAdmins() throws StorageException, ValidationException {
    // action
    instance.getVREAuthorization(VRE_ID, USER);

    // verify
    verifyVREAuthorizationIsSearchedFor(VRE_ID, USER_ID);
    verifyAVREAuthorizationIsCreatedFor(VRE_ID, USER_ID, UNVERIFIED_USER_ROLE);
    verifyNoEmailIsSend();
  }

  private void verifyNoEmailIsSend() {
    verifyZeroInteractions(mailSenderMock);

  }

  private void verifyVREAuthorizationIsSearchedFor(String vreId, String userId) {
    // VREAuthorization are equal when the vreId and userId are equal.
    verify(userConfigurationHandlerMock).findVREAuthorization(new VREAuthorization(VRE_ID, USER_ID));
  }

  private void findVREAuthorizationFor(String vreId, String userId) {
    VREAuthorization vreAuthorization = new VREAuthorization(VRE_ID, USER_ID);
    when(userConfigurationHandlerMock.findVREAuthorization(vreAuthorization)).thenReturn(vreAuthorization);
  }

  private void findAnAdminOfVRE(String vreId) {
    VREAuthorization example = new VREAuthorization(VRE_ID, null, ADMIN_ROLE);
    String adminUserId = "test";
    VREAuthorization foundAuthorization = new VREAuthorization(VRE_ID, adminUserId, ADMIN_ROLE);
    when(userConfigurationHandlerMock.findVREAuthorization(example)).thenReturn(foundAuthorization);
    User adminUser = new User();
    adminUser.setEmail("test@test.com");
    when(userConfigurationHandlerMock.getUser(adminUserId)).thenReturn(adminUser);
  }

  private void verifyAnEmailIsSendToAnAdminOfTheVRE() {
    verify(mailSenderMock).sendMail(anyString(), anyString(), anyString());
  }

  private void verifyAVREAuthorizationIsCreatedFor(String vreId, String userId, String unverifiedUserRole) throws StorageException, ValidationException {
    verify(userConfigurationHandlerMock).addVREAuthorization(new VREAuthorization(vreId, userId, unverifiedUserRole));
  }
}
