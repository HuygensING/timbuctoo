package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Vre {
  private final String vreName;
  private final LinkedHashMap<String, Collection> collections = Maps.newLinkedHashMap();

  Vre(String vreName) {
    this.vreName = vreName;
  }

  public Collection getCollectionForTypeName(String entityTypeName) {
    return collections.get(entityTypeName);
  }

  public Optional<Collection> getCollectionForCollectionName(String collectionName) {
    return this.collections.values().stream()
      .filter(x-> Objects.equals(x.getCollectionName(), collectionName))
      .findAny();
  }



  public Set<String> getEntityTypes() {
    return collections.keySet();
  }

  public String getVreName() {
    return vreName;
  }

  public Optional<Collection> getImplementerOf(String abstractType) {
    return this.collections.values().stream()
      .filter(x-> Objects.equals(x.getAbstractType(), abstractType))
      .findAny();
  }

  public String getOwnType(String... types) {
    Iterator<String> intersection = Sets.intersection(collections.keySet(), Sets.newHashSet(types)).iterator();
    if (intersection.hasNext()) {
      return intersection.next();
    } else {
      return null;
    }
  }

  public void addCollection(Collection collection) {
    collections.put(collection.getEntityTypeName(), collection);
  }

  public Map<String, Collection> getCollections() {
    return collections;
  }
}
