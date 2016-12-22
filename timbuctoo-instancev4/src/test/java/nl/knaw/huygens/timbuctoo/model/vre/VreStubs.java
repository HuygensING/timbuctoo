package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.core.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;

public class VreStubs {
  public static Vre withName(String vreName) {
    return new Vre(vreName, Maps.newHashMap());
  }

  public static Vre vreWithNameAndCollection(DataStoreOperations dataStoreOperations, String name,
                                             CreateCollection createCollection) {
    Vre vre = dataStoreOperations.ensureVreExists(name);
    CreateCollection collection = createCollection;
    dataStoreOperations.addCollectionToVre(vre, collection);
    Vre returnValue = dataStoreOperations.loadVres().getVre(name);
    return returnValue;
  }
}
