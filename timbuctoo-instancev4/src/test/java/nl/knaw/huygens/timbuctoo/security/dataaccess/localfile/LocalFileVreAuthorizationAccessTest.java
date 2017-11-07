package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static nl.knaw.huygens.timbuctoo.util.FileHelpers.makeTempDir;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.UNVERIFIED_USER_ROLE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class LocalFileVreAuthorizationAccessTest {

  private Path authorizationsFolder;
  public static final String VRE = "vre";
  public static final String VRE_FILE = "vre.json";
  private Path vreAuthPath;

  public static final String USER_ID = "USER000000000001";
  public static final String USER_ID_WITHOUT_WRITE_PERMISSIONS = "USER000000000002";
  private VreAuthorizationAccess instance;
  private ObjectMapper objectMapper;

  @Before
  public void setup() throws Exception {
    authorizationsFolder = makeTempDir();
    vreAuthPath = authorizationsFolder.resolve(VRE_FILE);

    VreAuthorization[] authorizations = {
      VreAuthorization.create(VRE, USER_ID, "USER"),
      VreAuthorization.create(VRE, USER_ID_WITHOUT_WRITE_PERMISSIONS, UNVERIFIED_USER_ROLE)
    };
    File file = vreAuthPath.toFile();
    objectMapper = new ObjectMapper();
    objectMapper.writeValue(file, authorizations);

    instance = new LocalFileVreAuthorizationAccess(authorizationsFolder);
  }

  @After
  public void teardown() throws Exception {
    if (new File(vreAuthPath.toString()).exists()) {
      Files.delete(vreAuthPath);
    }
  }

  @Test
  public void authorizationForReturnsAnOptionalOfTheFoundVreAuthorization() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.getAuthorization(VRE, USER_ID);

    assertThat(vreAuthorization, is(present()));
  }

  @Test
  public void authorizationForReturnsAnEmptyOptionalIfTheVreAuthorizationIsNotFound() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.getAuthorization(VRE, "unknownUser");

    assertThat(vreAuthorization, is(not(present())));
  }

  @Test
  public void authorizationForReturnsAnEmptyOptionalIfTheVreAuthorizationsFileDoesNotExist() throws Exception {
    Optional<VreAuthorization> vreAuthorization = instance.getAuthorization("nonExisting", USER_ID);

    assertThat(vreAuthorization, is(not(present())));
  }

  // TODO: add a test to check how to handle an IOException

  @Test
  public void addAuthorizationForAddsANewAuthorizationToTheVreForTheUser() throws Exception {
    String unknownUser = "unknownUser";
    Optional<VreAuthorization> authorization = instance.getAuthorization(VRE, unknownUser);
    assertThat(authorization, is(not(present())));

    instance.getOrCreateAuthorization(VRE, unknownUser, UNVERIFIED_USER_ROLE);

    Optional<VreAuthorization> authorization1 = instance.getAuthorization(VRE, unknownUser);
    assertThat(authorization1, is(present()));
  }

  @Test
  public void addAuthorizationReturnsTheAddedAuthorization() throws Exception {
    String unknownUser = "unknownUser";

    VreAuthorization vreAuthorization = instance.getOrCreateAuthorization(VRE, unknownUser, UNVERIFIED_USER_ROLE);
    Optional<VreAuthorization> authorization = instance.getAuthorization(VRE, unknownUser);

    assertThat(vreAuthorization, is(authorization.get()));
  }

  @Test
  public void addAuthorizationIgnoresTheSecondAdditionForUserToAVre() throws Exception {
    String unknownUser = "unknownUser";

    instance.getOrCreateAuthorization(VRE, unknownUser, UNVERIFIED_USER_ROLE);
    VreAuthorization authorization2 = instance.getOrCreateAuthorization(VRE, unknownUser, ADMIN_ROLE);

    assertThat(authorization2.getRoles(), contains(UNVERIFIED_USER_ROLE));

  }

  @Test
  public void addAuthorizationCreatesANewFileForAnUnknownVre() throws Exception {
    String newVre = "newVRE";
    Optional<VreAuthorization> authorization = instance.getAuthorization(newVre, USER_ID);
    assertThat(authorization, is(not(present())));

    VreAuthorization createAuthorization = instance.getOrCreateAuthorization(newVre, USER_ID, UNVERIFIED_USER_ROLE);

    assertThat(createAuthorization, is(not(nullValue())));
    Optional<VreAuthorization> authorization1 = instance.getAuthorization(newVre, USER_ID);
    assertThat(authorization1, is(present()));

    // Teardown
    Files.delete(authorizationsFolder.resolve(String.format("%s.json", newVre)));
  }

  @Test(expected = AuthorizationUnavailableException.class)
  public void deleteVreAuthorizationThrowsAnAuthorizationUnavailableExceptionWhenTheFileIsUnavailable()
    throws Exception {

    instance.deleteVreAuthorizations("nonExisting");
  }

  @Test
  public void deleteVreAuthorizationsDeletesTheVreAuthorizationsFile() throws Exception {
    instance.deleteVreAuthorizations(VRE);
    assertThat(new File(authorizationsFolder.resolve(VRE_FILE).toString()).exists(), equalTo(false));
  }
}
