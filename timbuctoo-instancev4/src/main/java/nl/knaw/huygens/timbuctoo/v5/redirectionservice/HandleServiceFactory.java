package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import nl.knaw.huygens.persistence.PersistenceManager;


public class HandleServiceFactory implements RedirectionServiceFactory {
  private final PersistenceManager manager;

  @JsonCreator
  public HandleServiceFactory(@JsonProperty("useDummy") Boolean useDummy,
                              @JsonProperty("privateKeyFile") String privateKeyFile,
                              @JsonProperty("cypher") String cypher,
                              @JsonProperty("namingAuthority") String namingAuthority,
                              @JsonProperty("prefix") String prefix) {

    PersistenceManagerFactory persistenceManagerFactory = new PersistenceManagerFactory(
      useDummy,
      privateKeyFile,
      cypher,
      namingAuthority,
      prefix
    );

    manager = persistenceManagerFactory.build();
  }

  @Override
  public RedirectionService makeRedirectionService(ActiveMQBundle activeMqBundle) {
    HandleService handleService = new HandleService(manager, activeMqBundle);

    return handleService;
  }
}

