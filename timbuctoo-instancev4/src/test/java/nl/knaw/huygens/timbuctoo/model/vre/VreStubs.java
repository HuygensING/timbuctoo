package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.core.CollectionNameHelper;
import nl.knaw.huygens.timbuctoo.core.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;

public class VreStubs {
  public static Vre withName(String vreName) {
    return new Vre(vreName, Maps.newHashMap());
  }

  public static Vre minimalCorrectVre(DataStoreOperations dataStoreOperations, String name) {
    Vre admin = dataStoreOperations.ensureVreExists("Admin");
    dataStoreOperations.addCollectionToVre(admin, CreateCollection.defaultCollection());


    Vre vre = dataStoreOperations.ensureVreExists(name);
    dataStoreOperations.addCollectionToVre(vre, CreateCollection.defaultCollection());

    Vres vres = dataStoreOperations.loadVres();

    Collection collection =
      vres.getVre(name).getCollectionForCollectionName(CollectionNameHelper.defaultCollectionName(vre)).get();

    Collection adminCollection =
      vres.getVre("Admin").getCollectionForCollectionName(CollectionNameHelper.defaultCollectionName(admin)).get();

    dataStoreOperations.setAdminCollection(collection, adminCollection);

    return dataStoreOperations.loadVres().getVre(name);
  }
}
