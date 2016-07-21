package nl.knaw.huygens.timbuctoo.model.vre;

import java.util.Map;
import java.util.Optional;

public interface Vres {

  Optional<Collection> getCollection(String collection);

  Optional<Collection> getCollectionForType(String type);

  Vre getVre(String vre);

  Map<String, Vre> getVres();

  Map<String, Map<String, String>> getKeywordTypes();

  void reload();
}
