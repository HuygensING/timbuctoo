package nl.knaw.huygens.timbuctoo.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import nl.knaw.huygens.persistence.DefaultPersistenceManager;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Gremlin;
import org.slf4j.Logger;

public class HandleManagerFactory {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Gremlin.class);

  @JsonProperty
  private Boolean useDummy;

  @JsonProperty
  private String privateKeyFile;
  @JsonProperty
  private String cypher;
  @JsonProperty
  private String namingAuthority;
  @JsonProperty
  private String prefix;

  public PersistenceManager build() {
    if (useDummy == null) {
      if (Strings.isNullOrEmpty(privateKeyFile) ||
        Strings.isNullOrEmpty(cypher) ||
        Strings.isNullOrEmpty(namingAuthority) ||
        Strings.isNullOrEmpty(prefix)) {
        LOG.error("Configuration must have either: \n" +
          "\n" +
          "    persistenceManager:\n" +
          "      useDummy: on\n" +
          "\n" +
          "or\n" +
          "\n" +
          "    persistenceManager:\n" +
          "      privateKeyFile: ...\n" +
          "      cypher: ...\n" +
          "      namingAuthority: ...\n" +
          "      prefix: ...\n");
        throw new IllegalArgumentException(
          "'useDummy' must be yes or else 'privateKeyFile', 'cypher', 'namingAuthority' and 'prefix' must be provided"
        );
      }
    }
    if (useDummy != null && useDummy) { //must compare to true because useDummy might be null
      LOG.info("Using dummy persistence manager instead of real handle server");
      return new DefaultPersistenceManager();
    } else {
      LOG.info("Using real handle server");
      try {
        return HandleManager.newHandleManager(
          cypher,
          namingAuthority,
          prefix,
          privateKeyFile
        );
      } catch (PersistenceManagerCreationException e) {
        //this factory is written for use in the dropwizard configuration class where you can specify with @notnull if a
        //property's existence should prevent the software from starting.
        //
        //Otherwise I would have used optional.
        return null;
      }
    }

  }
}
