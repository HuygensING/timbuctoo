package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class AuthorizationMigration {

  private static final ObjectMapper OBJECT_MAPPER;
  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationMigration.class);

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.registerModule(new GuavaModule());
  }

  private final String authorizationsPath;
  private final UserValidator userValidator;
  private final String dataSetPath;

  public AuthorizationMigration(String authorizationsPath, UserValidator userValidator, String dataSetPath) {
    this.authorizationsPath = authorizationsPath;
    this.userValidator = userValidator;
    this.dataSetPath = dataSetPath;
  }

  public void migrate() throws Exception {
    Path path = Paths.get(authorizationsPath);

    if (!path.toFile().exists()) {
      return;
    }

    Set<Path> authFiles = Files.walk(path)
                               .filter(file -> file.toFile().getName().endsWith(".json") && file.toFile().isFile())
                               .collect(toSet());

    for (Path authFile : authFiles) {
      Set<VreAuthorization> oldAuths = OBJECT_MAPPER.readValue(
        authFile.toFile(),
        new TypeReference<Set<VreAuthorization>>() {
        }
      );

      Set<VreAuthorization> newAuths = Sets.newHashSet();
      for (VreAuthorization oldAuth : oldAuths) {
        Optional<User> userFromPersistentId = userValidator.getUserFromUserId(oldAuth.getUserId());
        String userId = oldAuth.getUserId();
        if (userFromPersistentId.isPresent()) {
          userId = userFromPersistentId.get().getPersistentId();
        } else {
          LOG.warn("No user found with id '{}'", oldAuth.getUserId());
        }
        newAuths.add(VreAuthorization.create(oldAuth.getVreId(), userId, oldAuth.getRoles().toArray(new String[]{})));
      }

      OBJECT_MAPPER.writeValue(createOuputFile(authFile), newAuths);
    }

  }

  private File createOuputFile(Path authFile) {
    String name = authFile.getName(authFile.getNameCount() - 1).toString();
    String fileWithoutExtension = name.substring(0, name.indexOf("."));

    /*
     * The directory of the general authorizations.json will be in the root of the dataSets folder.
     * The authorizations of the specific data set will be added to the folder of the data set.
     * For legacy data sets this means {dataSetsPath}/{dataSetName}.
     * For new data sets this will be {dataSetsPath}/{ownerId}/{dataSetName}.
     */
    File directory;
    if (name.equals("authorizations.json")) {
      directory = Paths.get(dataSetPath).toFile();
    } else if (fileWithoutExtension.contains("__")) {
      Tuple<String, String> ownerIdDataSetId = DataSetMetaData.splitCombinedId(fileWithoutExtension);
      directory = Paths.get(
        dataSetPath,
        ownerIdDataSetId.getLeft(),
        ownerIdDataSetId.getRight().replace("__", "")
      ).toFile();
    } else {
      directory = Paths.get(
        dataSetPath,
        fileWithoutExtension
      ).toFile();
    }

    directory.mkdirs();
    return new File(directory, "authorizations.json");
  }
}
