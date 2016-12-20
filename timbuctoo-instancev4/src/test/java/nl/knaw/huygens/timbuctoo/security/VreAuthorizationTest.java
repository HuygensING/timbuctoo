package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.UNVERIFIED_USER_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.USER_ROLE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VreAuthorizationTest {
  @Test
  public void isAllowedToWriteReturnsTrueIfTheRolesContainUser() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null, USER_ROLE);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(true));
  }

  @Test
  public void isAllowedToWriteReturnsTrueIfTheRolesContainAdmin() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null, ADMIN_ROLE);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(true));
  }

  @Test
  public void isAllowedToWriteReturnsFalseIfTheRolesOnlyContainUnverifiedUser() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null, UNVERIFIED_USER_ROLE);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(false));
  }

  @Test
  public void isAllowedToWriteReturnsTrueIfTheRolesContainUnverifiedUserAndUser() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null, UNVERIFIED_USER_ROLE, USER_ROLE);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(true));
  }

  @Test
  public void isAllowedToWriteReturnsTrueIfTheRolesContainUnverifiedUserAndAdmin() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null, UNVERIFIED_USER_ROLE, ADMIN_ROLE);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(true));
  }

  @Test
  public void isAllowedToWriteReturnsFalseIfTheRolesAreEmpty() throws Exception {
    VreAuthorization instance = new VreAuthorization(null, null);

    boolean allowedToWrite = instance.isAllowedToWrite();

    assertThat(allowedToWrite, is(false));
  }

}
