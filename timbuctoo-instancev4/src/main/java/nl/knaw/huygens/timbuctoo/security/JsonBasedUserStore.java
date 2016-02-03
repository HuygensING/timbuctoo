package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JsonBasedUserStore {

  public static final Logger LOG = LoggerFactory.getLogger(JsonBasedUserStore.class);
  private final Path usersFile;
  private final ObjectMapper objectMapper;

  public JsonBasedUserStore(Path usersFile) {
    this.usersFile = usersFile;
    objectMapper = new ObjectMapper();
  }

  public Optional<User> userFor(String pid) throws AuthenticationUnavailableException {
    List<User> users = null;
    try {
      users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
      });
    } catch (IOException e) {
      LOG.error("Cannot read {}", usersFile.toAbsolutePath());
      LOG.error("Exception thrown", e);
      throw new AuthenticationUnavailableException(e.getMessage());
    }

    return users.stream().filter(user -> user.getPersistentId().equals(pid)).findFirst();
  }

  public Optional<User> userForId(String userId) throws AuthenticationUnavailableException {
    List<User> users;
    try {
      users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
      });
    } catch (IOException e) {
      LOG.error("Cannot read {}", usersFile.toAbsolutePath());
      LOG.error("Exception thrown", e);
      throw new AuthenticationUnavailableException(e.getMessage());
    }

    return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
  }
}
