package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.google.common.base.Strings;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.timbuctoo.handle.DummyPersistenceManager;
import org.slf4j.Logger;

public class PersistenceManagerFactory {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PersistenceManagerFactory.class);

  private Boolean useDummy;
  private String privateKeyFile;
  private String cypher;
  private String namingAuthority;
  private String prefix;

  public PersistenceManagerFactory(Boolean useDummy, String privateKeyFile, String cypher,
                                   String namingAuthority, String prefix) {
    this.useDummy = useDummy;
    this.privateKeyFile = privateKeyFile;
    this.cypher = cypher;
    this.namingAuthority = namingAuthority;
    this.prefix = prefix;
  }

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
    if (useDummy != null && useDummy) {
      LOG.info("Using dummy persistence manager instead of real handle server");
      return new DummyPersistenceManager();
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
