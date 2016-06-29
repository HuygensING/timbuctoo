package nl.knaw.huygens.timbuctoo.model.vre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Vre {
  private static final Logger LOG = LoggerFactory.getLogger(Vre.class);

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

  public Optional<Collection> getRelationCollection() {
    Iterator<Collection> collectionIt = getCollections()
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().isRelationCollection())
      .map(Map.Entry::getValue)
      .iterator();

    return collectionIt.hasNext() ? Optional.of(collectionIt.next()) : Optional.empty();
  }

  public void addCollection(Collection collection) {
    collections.put(collection.getEntityTypeName(), collection);
  }

  public Map<String, Collection> getCollections() {
    return collections;
  }

  public void persistToDatabase(GraphWrapper graphWrapper, Optional<Map<String, String>> keywordTypes) {
    LOG.info("Persisting vre '{}' to database", vreName);
    Graph graph = graphWrapper.getGraph();

    // Look for existing VRE vertex
    GraphTraversal<Vertex, Vertex> existing = graph.traversal().V().hasLabel("VRE").has("name", vreName);

    Vertex vreVertex;
    // Create new if does not exist
    if (existing.hasNext()) {
      vreVertex = existing.next();
      LOG.info("Replacing existing vertex {}.", vreVertex);
    } else {
      vreVertex = graph.addVertex("VRE");
      LOG.info("Creating new vertex");
    }

    // Add properties
    vreVertex.property("name", vreName);
    if (keywordTypes.isPresent()) {
      try {
        vreVertex.property("keywordTypes", new ObjectMapper().writeValueAsString(keywordTypes.get()));
      } catch (JsonProcessingException e) {
        LOG.error("Failed to serialize keyword types to JSON {}", keywordTypes.get());
      }
    }

    // Add relations and child collections
  }
}
