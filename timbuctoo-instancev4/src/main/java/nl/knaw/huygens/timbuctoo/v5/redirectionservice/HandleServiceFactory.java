package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueManager;
import org.slf4j.Logger;


public class HandleServiceFactory implements RedirectionServiceFactory {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HandleServiceFactory.class);
  private PersistenceManager manager;

  @JsonCreator
  public HandleServiceFactory(@JsonProperty("privateKeyFile") String privateKeyFile,
                              @JsonProperty("cypher") String cypher,
                              @JsonProperty("namingAuthority") String namingAuthority,
                              @JsonProperty("prefix") String prefix) {

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
        manager =  HandleManager.newHandleManager(
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
        manager = null;
      }

    }
  }

  @Override
  public RedirectionService makeRedirectionService(QueueManager queueManager, DataSetRepository dataSetRepository) {
    return new HandleService(manager, queueManager, dataSetRepository);
  }
}

