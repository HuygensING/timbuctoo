package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.security.LegacyLogin;
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

      Login newLogin = new Login(login.getUserPid(), login.getUsername(), login.getPassword().getBytes(),
              login.getSalt());

      newLogin.setGivenName(login.getGivenName());
      newLogin.setEmailAddress(login.getEmailAddress());
      newLogin.setOrganization(login.getOrganization());
      newLogin.setId(login.getId());
      newLogin.setRev(login.getRev());
      newLogin.setModified(login.getModified());
      newLogin.setCreated(login.getCreated());
      newLogins.add(newLogin);
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
