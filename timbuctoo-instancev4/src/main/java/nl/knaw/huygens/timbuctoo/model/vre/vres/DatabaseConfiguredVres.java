package nl.knaw.huygens.timbuctoo.model.vre.vres;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DatabaseConfiguredVres implements Vres {

  private final GraphWrapper graphWrapper;
  private ConfiguredVres loadedInstance;

  public DatabaseConfiguredVres(GraphWrapper wrapper) {
    this.graphWrapper = wrapper;
  }

  public Optional<Collection> getCollection(String collection) {
    return getLoadedInstance().getCollection(collection);
  }

  public Optional<Collection> getCollectionForType(String type) {
    return getLoadedInstance().getCollectionForType(type);
  }

  public Vre getVre(String vre) {
    return getLoadedInstance().getVre(vre);
  }

  public Map<String, Vre> getVres() {
    return getLoadedInstance().getVres();
  }

  private Vres getLoadedInstance() {
    if (loadedInstance != null) {
      return loadedInstance;
    }
    reload();
    return loadedInstance;
  }

  @Override
  public void reload() {
    final List<Vre> vreList = new ArrayList<>();
    final Map<String, Map<String, String>> keywordTypes = new HashMap<>();

    graphWrapper.getGraph().traversal().V().hasLabel(Vre.DATABASE_LABEL).forEachRemaining(vreVertex -> {
      final Vre vre = Vre.load(vreVertex);
      vreList.add(vre);
      keywordTypes.put(vre.getVreName(), vre.getKeywordTypes());
    });

    loadedInstance = new ConfiguredVres(vreList);
  }
}
