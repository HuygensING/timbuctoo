package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.queue.QueueManager;


public class HandleServiceFactory implements RedirectionServiceFactory {
  private final PersistenceManager manager;


  @JsonCreator
  public HandleServiceFactory(@JsonProperty("privateKeyFile") String privateKeyFile,
                              @JsonProperty("cypher") String cypher,
                              @JsonProperty("namingAuthority") String namingAuthority,
                              @JsonProperty("prefix") String prefix) {

    HandlePersistenceManagerFactory handleManagerFactory = new HandlePersistenceManagerFactory(
      privateKeyFile,
      cypher,
      namingAuthority,
      prefix
    );

    manager = handleManagerFactory.build();
  }

  @Override
  public RedirectionService makeRedirectionService(QueueManager queueManager, DataSetRepository dataSetRepository) {
    HandleService handleService = new HandleService(manager, queueManager, dataSetRepository);

    return handleService;
  }
}

