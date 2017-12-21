package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.Files.createTempDir;
import static nl.knaw.huygens.timbuctoo.security.dto.UserStubs.userWithPid;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthorizationMigrationTest {

  private static final ObjectMapper OBJECT_MAPPER;
  private static final String USER_PID = "userPid";
  private static final String USER_ID = "userId";

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.registerModule(new GuavaModule());
  }

  @Test
  public void migrateTransformsTheAuthorizationsToAuthorizationsForUserId() throws Exception {
    File authorizationsDir = createTempDir();
    File outputDir = createTempDir();
    createOldVreAuthFile(authorizationsDir, "u33707283d426f900d4d33707283d426f900d4d0d____charter.json");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromUserId(USER_ID)).willReturn(
      Optional.of(userWithPid(USER_PID))
    );

    AuthorizationMigration instance = new AuthorizationMigration(
      authorizationsDir.toString(),
      userValidator,
      outputDir.getAbsolutePath()
    );

    instance.migrate();

    List<VreAuthorization> auths = getNewVreAuthorizations(
      new File(outputDir.getAbsolutePath(), "u33707283d426f900d4d33707283d426f900d4d0d/charter/authorizations.json")
    );
    assertThat(auths, contains(allOf(
      hasProperty("vreId", is("u33707283d426f900d4d33707283d426f900d4d0d__charter")),
      hasProperty("userId", is(USER_PID)),
      hasProperty("roles", contains("ADMIN"))
    )));
  }

  @Test
  public void migratePutsTheFilesTheFolderOfTheDataSet() throws Exception {
    File authorizationsDir = createTempDir();
    File outputDir = createTempDir();
    createOldVreAuthFile(authorizationsDir, "legacydataset.json");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromUserId(USER_ID)).willReturn(
      Optional.of(userWithPid(USER_PID))
    );

    AuthorizationMigration instance = new AuthorizationMigration(
      authorizationsDir.toString(),
      userValidator,
      outputDir.getAbsolutePath()
    );

    instance.migrate();

    List<VreAuthorization> auths = getNewVreAuthorizations(
      new File(outputDir.getAbsolutePath(), "legacydataset/authorizations.json")
    );
    assertThat(auths, contains(allOf(
      hasProperty("vreId", is("u33707283d426f900d4d33707283d426f900d4d0d__charter")),
      hasProperty("userId", is(USER_PID)),
      hasProperty("roles", contains("ADMIN"))
    )));
  }

  @Test
  public void migratePutsTheGeneralAuthorizationsFileToRootOfTheDataSetsDir() throws Exception {
    File authorizationsDir = createTempDir();
    File dataSetsDir = createTempDir();
    createOldVreAuthFile(authorizationsDir, "authorizations.json");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromUserId(USER_ID)).willReturn(
      Optional.of(userWithPid(USER_PID))
    );

    AuthorizationMigration instance = new AuthorizationMigration(
      authorizationsDir.toString(),
      userValidator,
      dataSetsDir.getAbsolutePath()
    );

    instance.migrate();

    List<VreAuthorization> auths = getNewVreAuthorizations(
      new File(dataSetsDir.getAbsolutePath(), "authorizations.json")
    );
    assertThat(auths, contains(allOf(
      hasProperty("vreId", is("u33707283d426f900d4d33707283d426f900d4d0d__charter")),
      hasProperty("userId", is(USER_PID)),
      hasProperty("roles", contains("ADMIN"))
    )));
  }

  private List<VreAuthorization> getNewVreAuthorizations(File filePath) throws IOException {
    return OBJECT_MAPPER.readValue(filePath, new TypeReference<List<VreAuthorization>>() {
    });
  }

  private void createOldVreAuthFile(File authorizationsDir, String fileName) throws IOException {
    File inputAuthFile = new File(authorizationsDir, fileName);
    FileWriter fileWriter = new FileWriter(inputAuthFile);
    fileWriter.write(
      "[{\"vreId\":\"u33707283d426f900d4d33707283d426f900d4d0d__charter\"," +
        "\"userId\":\"" + USER_ID + "\",\"roles\":[\"ADMIN\"]}]"
    );
    fileWriter.flush();
    fileWriter.close();
  }

}
