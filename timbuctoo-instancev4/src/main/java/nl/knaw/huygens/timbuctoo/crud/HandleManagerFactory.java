package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.persistence.DefaultPersistenceManager;
import nl.knaw.huygens.persistence.HandleManager;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;

public class HandleManagerFactory {
  @JsonProperty
  private boolean useDummy = false;

  @JsonProperty
  private String privateKeyFile;
  @JsonProperty
  private String cypher;
  @JsonProperty
  private String namingAuthority;
  @JsonProperty
  private String prefix;

  public PersistenceManager build() {
    if (!useDummy) {
      return new DefaultPersistenceManager();
    } else {
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
