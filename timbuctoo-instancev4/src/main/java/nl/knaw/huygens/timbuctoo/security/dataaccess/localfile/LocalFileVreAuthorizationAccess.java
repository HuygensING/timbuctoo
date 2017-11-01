package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.EscapeFunnyCharacters.escapeFunnyCharacters;

public class LocalFileVreAuthorizationAccess implements VreAuthorizationAccess {
  private final ObjectMapper objectMapper;
  private final Path authorizationsFolder;

  public LocalFileVreAuthorizationAccess(Path authorizationsFolder) {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    this.authorizationsFolder = authorizationsFolder;
  }

  @Override
  public VreAuthorization getOrCreateAuthorization(String vreId, String userId, String userRole)
    throws AuthorizationUnavailableException {
    Optional<VreAuthorization> authOptional = getAuthorization(vreId, userId);

    if (authOptional.isPresent()) {
      return authOptional.get();
    } else {
      try {
        synchronized (authorizationsFolder) {
          File file = getFile(vreId);
          List<VreAuthorization> authorizations = Lists.newArrayList();
          if (file.exists()) {
            authorizations =
              objectMapper.readValue(file, new TypeReference<List<VreAuthorization>>() {
              });
          }
          VreAuthorization vreAuthorization = VreAuthorization.create(vreId, userId, userRole);
          authorizations.add(vreAuthorization);

          objectMapper.writeValue(file, authorizations.toArray(new VreAuthorization[authorizations.size()]));
          return vreAuthorization;
        }
      } catch (IOException e) {
        throw new AuthorizationUnavailableException(e.getMessage());
      }
    }
  }


  @Override
  public Optional<VreAuthorization> getAuthorization(String vreId, String userId)
    throws AuthorizationUnavailableException {

    File file = getFile(vreId);

    if (!file.exists()) {
      return Optional.empty();
    }

    try {
      List<VreAuthorization> authorizations;
      synchronized (authorizationsFolder) {
        authorizations =
          objectMapper.readValue(file, new TypeReference<List<VreAuthorization>>() {
          });
      }
      return authorizations.stream()
        .filter(authorization -> Objects.equals(authorization.getUserId(), userId))
        .findAny();
    } catch (IOException e) {
      throw new AuthorizationUnavailableException(e.getMessage());
    }

  }

  @Override
  public void deleteVreAuthorizations(String vreId) throws AuthorizationUnavailableException {
    synchronized (authorizationsFolder) {
      if (!getFile(vreId).delete()) {
        throw new AuthorizationUnavailableException("Failed to delete vre authorizations for vre '" + vreId + "'");
      }
    }
  }

  private File getFile(String vreId) {
    return authorizationsFolder.resolve(String.format("%s.json", escapeFunnyCharacters(vreId))).toFile();
  }

}
