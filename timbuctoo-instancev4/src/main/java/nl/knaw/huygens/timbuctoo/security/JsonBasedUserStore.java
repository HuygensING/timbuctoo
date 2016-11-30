package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JsonBasedUserStore implements UserStore, UserCreator {

  public static final Logger LOG = LoggerFactory.getLogger(JsonBasedUserStore.class);
  private final Path usersFile;
  private final ObjectMapper objectMapper;

  public JsonBasedUserStore(Path usersFile) {
    this.usersFile = usersFile;
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public Optional<User> userFor(String pid) throws AuthenticationUnavailableException {
    List<User> users;
    try {
      synchronized (usersFile) {
        users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
        });
      }
    } catch (IOException e) {
      LOG.error("Cannot read {}", usersFile.toAbsolutePath());
      LOG.error("Exception thrown", e);
      throw new AuthenticationUnavailableException(e.getMessage());
    }

    return users.stream().filter(user -> Objects.equals(user.getPersistentId(),pid)).findFirst();
  }

  @Override
  public Optional<User> userForId(String userId) throws AuthenticationUnavailableException {
    List<User> users;
    try {
      synchronized (usersFile) {
        users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
        });
      }
    } catch (IOException e) {
      LOG.error("Cannot read {}", usersFile.toAbsolutePath());
      LOG.error("Exception thrown", e);
      throw new AuthenticationUnavailableException(e.getMessage());
    }

    return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
  }

  @Override
  public User saveNew(String displayName, String persistentId) throws AuthenticationUnavailableException {
    List<User> users;
    User nw = new User(displayName);
    nw.setPersistentId(persistentId);
    try {
      synchronized (usersFile) {
        users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
        });
      }
      users.add(nw);
      objectMapper.writeValue(usersFile.toFile(), users.toArray(new User[users.size()]));
    } catch (IOException e) {
      LOG.error("Cannot read {}", usersFile.toAbsolutePath());
      LOG.error("Exception thrown", e);
      throw new AuthenticationUnavailableException(e.getMessage());
    }
    return nw;
  }

  @Override
  public String createUser(String pid, String email, String givenName, String surname, String organization)
    throws UserCreationException {
    User user = new User();
    user.setPersistentId(pid);
    user.setDisplayName(String.format("%s %s", givenName, surname));
    try {
      synchronized (usersFile) {
        List<User> users = objectMapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
        });

        Optional<User> existingUser = users.stream().filter(u -> Objects.equals(u.getPersistentId(), pid)).findFirst();
        if (existingUser.isPresent()) {
          return existingUser.get().getId();
        }

        users.add(user);
        objectMapper.writeValue(usersFile.toFile(), users.toArray(new User[users.size()]));
      }
    } catch (IOException e) {
      throw new UserCreationException(e);
    }
    return user.getId();
  }
}
