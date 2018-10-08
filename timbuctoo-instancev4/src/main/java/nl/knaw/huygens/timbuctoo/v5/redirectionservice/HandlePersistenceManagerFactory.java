package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.google.common.base.Strings;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import org.slf4j.Logger;

public class HandlePersistenceManagerFactory {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandlePersistenceManagerFactory.class);
  private String privateKeyFile;
  private String cypher;
  private String namingAuthority;
  private String prefix;

  public HandlePersistenceManagerFactory(String privateKeyFile, String cypher,
                                         String namingAuthority, String prefix) {
    this.privateKeyFile = privateKeyFile;
    this.cypher = cypher;
    this.namingAuthority = namingAuthority;
    this.prefix = prefix;
  }

  public PersistenceManager build() {
    if (Strings.isNullOrEmpty(privateKeyFile) ||
      Strings.isNullOrEmpty(cypher) ||
      Strings.isNullOrEmpty(namingAuthority) ||
      Strings.isNullOrEmpty(prefix)) {
      LOG.error("Configuration must have: \n" +
        "    persistenceManager:\n" +
        "      privateKeyFile: ...\n" +
        "      cypher: ...\n" +
        "      namingAuthority: ...\n" +
        "      prefix: ...\n");
      throw new IllegalArgumentException(
        "'privateKeyFile', 'cypher', 'namingAuthority' and 'prefix' must be provided"
      );
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
