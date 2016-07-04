package nl.knaw.huygens.timbuctoo.model.vre.vres;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public class DatabaseConfiguredVres implements Vres {

  private final GraphWrapper graphWrapper;

  private Map<String, Map<String, String>> keywordTypes = new HashMap<>();
  private Map<String, Collection> collections = new HashMap<>();
  private Map<String, Vre> vres = new HashMap<>();

  private boolean loaded = false;

  public DatabaseConfiguredVres(GraphWrapper wrapper) {
    this.graphWrapper = wrapper;
  }

  public Optional<Collection> getCollection(String collection) {
    if (!loaded) {
      this.load();
    }
    return Optional.ofNullable(collections.get(collection));
  }

  public Optional<Collection> getCollectionForType(String type) {
    if (!loaded) {
      this.load();
    }
    return collections.values().stream().filter(coll -> Objects.equals(coll.getEntityTypeName(), type)).findAny();
  }

  public Vre getVre(String vre) {
    if (!loaded) {
      this.load();
    }
    return vres.get(vre);
  }

  public Map<String, Vre> getVres() {
    if (!loaded) {
      this.load();
    }
    return vres;
  }

  @Override
  public Map<String, Map<String, String>> getKeywordTypes() {
    if (!loaded) {
      this.load();
    }
    return keywordTypes;
  }

  public void load() {
    List<Vre> vreList = new ArrayList<>();

    keywordTypes.clear();
    collections.clear();
    vres.clear();

    graphWrapper.getGraph().traversal().V().hasLabel("VRE").forEachRemaining(vreVertex -> {
      final Vre vre = Vre.load(vreVertex);
      vreList.add(vre);
      keywordTypes.put(vre.getVreName(), vre.getKeywordTypes());
    });

    loadFromVreList(vreList);
    loaded = true;
  }

  private void loadFromVreList(List<Vre> vres) {
    this.vres = vres.stream().collect(toMap(Vre::getVreName, vre1 -> vre1));
    vres.stream()
        .flatMap(vre -> vre.getCollections().values().stream())
        .forEach(collection -> {
          if (collections.containsKey(collection.getCollectionName())) {
            throw new RuntimeException("Collection was defined multiple times: " + collection.getCollectionName());
          }
          collections.put(collection.getCollectionName(), collection);
        });
  }
}
