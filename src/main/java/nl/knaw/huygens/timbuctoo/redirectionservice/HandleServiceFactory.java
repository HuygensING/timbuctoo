package nl.knaw.huygens.timbuctoo.redirectionservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;

public class HandleServiceFactory implements RedirectionServiceFactory {
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
      System.err.println("""
          Configuration must have:\s
              persistenceManager:
                privateKeyFile: ...
                cypher: ...
                namingAuthority: ...
                prefix: ...
          """);
      throw new IllegalArgumentException(
        "'privateKeyFile', 'cypher', 'namingAuthority' and 'prefix' must be provided"
      );
    } else {
      System.out.println("Using real handle server");
      try {
        System.out.println("handle private key file " + privateKeyFile);
        manager =  HandleManager.newHandleManager(
          cypher,
          namingAuthority,
          prefix,
          privateKeyFile
        );
      } catch (PersistenceManagerCreationException e) {
        System.err.println("Creation of PersistenceManager failed");
        e.printStackTrace();
        //this factory is written for use in the dropwizard configuration class where you can specify with @notnull if a
        //property's existence should prevent the software from starting.
        //
        //Otherwise I would have used optional.
        manager = null;
      }

    }
  }

  @Override
  public RedirectionService makeRedirectionService(DataSetRepository dataSetRepository) {
    return new HandleService(manager, dataSetRepository);
  }
}

