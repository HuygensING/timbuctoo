package nl.knaw.huygens.timbuctoo.security.legacy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.security.dto.Login;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LoginFileMigration {

  private final ObjectMapper objectMapper;

  public LoginFileMigration() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public boolean isConverted(Path loginsFilePath) throws IOException {
    List<LegacyLogin> logins = objectMapper.readValue(loginsFilePath.toFile(),
            new TypeReference<List<LegacyLogin>>() {});

    for (LegacyLogin login : logins) {
      if (!CharMatcher.ASCII.matchesAllOf(login.getPassword())) {
        return false;
      }
    }

    return true;
  }

  public void convert(Path loginsFilePath, Path convertedLoginsFilePath) throws IOException {
    List<LegacyLogin> logins = objectMapper.readValue(loginsFilePath.toFile(),
            new TypeReference<List<LegacyLogin>>() {});

    List<Login> newLogins = Lists.newArrayList();

    for (LegacyLogin login : logins) {

      newLogins.add(Login.create(
        login.getUsername(),
        login.getUserPid(),
        login.getPassword().getBytes(),
        login.getSalt(),
        login.getGivenName(),
        login.getSurName(),
        login.getEmailAddress(),
        login.getOrganization())
      );
    }

    // ObjectMapper will only write the @type property for a typed array, not for a list, because
    // lists do not have knowledge of object type at runtime.
    Login[] loginArray = newLogins.toArray(new Login[newLogins.size()]);
    objectMapper.writeValue(convertedLoginsFilePath.toFile(), loginArray);
  }

  public void convert(Path loginsFilePath) throws IOException {
    convert(loginsFilePath, loginsFilePath);
  }
}
