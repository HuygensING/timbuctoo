package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.RelationDescription;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class TinkerpopSaver implements AutoCloseable, Saver {
  private final VertexByUniqueId vertexByUniqueId = new VertexByUniqueId();
  private final GraphWrapper wrapper;
  private final Vre vre;
  private final Map<String, RelationDescription> descriptions;
  private int saveCounter;
  private Transaction tx;
  private final int maxVerticesPerTransaction;
  private final Set<Collection> importedCollections = new HashSet<>();

  public TinkerpopSaver(GraphWrapper wrapper, Vre vre, Map<String, RelationDescription> descriptions,
                        int maxVerticesPerTransaction) {
    this.wrapper = wrapper;
    this.vre = vre;
    this.descriptions = descriptions;
    tx = wrapper.getGraph().tx();
    this.maxVerticesPerTransaction = maxVerticesPerTransaction;
  }

  private void allowCommit() {
    if (saveCounter++ > maxVerticesPerTransaction) {
      saveCounter = 0;
      tx.commit();
      tx = wrapper.getGraph().tx();
    }
  }

  @Override
  public void close() {
    tx.commit();
  }

  @Override
  public Vertex setVertexProperties(Collection collection, Optional<String> identityOpt,
                                    HashMap<String, Object> currentProperties) throws VertexCreatedTwiceException {
    allowCommit();
    Vertex result;
    if (identityOpt.isPresent()) {
      result = getOrCreateVertex(collection, identityOpt.get()).get();
    } else {
      result = wrapper.getGraph().addVertex();
    }

    if (result.property("rev").isPresent()) {
      throw new VertexCreatedTwiceException();
    }
    final String typeName = collection.getEntityTypeName();
    //FIXME re-use code from crudservice create
    result.property("rev", 1);
    result.property("tim_id", UUID.randomUUID().toString());
    result.property("types", jsnA(jsn(typeName)).toString());
    result.property("isLatest", true);
    currentProperties.forEach(result::property);
    ((Neo4jVertex) result).addLabel(typeName);

    identityOpt.ifPresent(uniqeValue -> {
      vertexByUniqueId.put(collection, uniqeValue, result);
    });
    return result;
  }

  private Optional<Vertex> getOrCreateVertex(Collection collection, String identity) {
    if (vertexByUniqueId.has(collection, identity)) {
      return Optional.of(vertexByUniqueId.get(collection, identity));
    } else {
      if (importedCollections.contains(collection)) {
        return Optional.empty();//collection was already imported and vertex was not registered
      } else {
        final Vertex vertex = wrapper.getGraph().addVertex();
        vertexByUniqueId.put(collection, identity, vertex);
        return Optional.of(vertex);
      }
    }
  }

  @Override
  public Vre getVre() {
    return vre;
  }

  @Override
  public Optional<String> makeRelation(Vertex from, String relationName, Collection ownCollection,
                                       String otherCollectionName, String value) {
    allowCommit();
    final RelationDescription description = descriptions.get(relationName);

    if (description == null) {
      //FIXME allow importing of relations
      return Optional.of("Relation " + relationName + " does not exist");
    } else {
      final Optional<Collection> otherCollectionOpt = vre.getCollectionForCollectionName(otherCollectionName);
      if (!otherCollectionOpt.isPresent()) {
        //FIXME create VRE while importing
        return Optional.of("Relation " + relationName + " does not exist");
      } else {
        Collection otherCollection = otherCollectionOpt.get();
        if (description.isValid(ownCollection, otherCollection)) {
          final Optional<Vertex> vertex = getOrCreateVertex(otherCollection, value);
          if (vertex.isPresent()) {
            description.makeRelation(from, vertex.get());
            return Optional.empty();
          } else {
            return Optional.of("The target vertex was not found");
          }
        } else {
          return Optional.of("This relation is not allowed from " + ownCollection.getCollectionName() +
            " to " + otherCollectionName);
        }
      }
    }
  }

  @Override
  public Optional<String> checkLeftoverVerticesThatWereExpected(Collection openCollection) {
    //check for any unmatched vertices
    //throw new UnsupportedOperationException("");//FIXME: implement
    return Optional.empty();
  }

  @Override
  public void markCollectionAsDone(Collection openCollection) {
    importedCollections.add(openCollection);
  }

  @Override
  public Optional<String> checkLeftoverCollectionsThatWereExpected() {
    //FIXME a
    // missing collection that was pointed to
    // also check all collections for missing pointers
    //throw new UnsupportedOperationException("");//FIXME: implement
    return Optional.empty();
  }

  @Override
  public Optional<String> checkRelationtypesThatWereExpected() {
    //throw new UnsupportedOperationException("");//FIXME: implement
    return Optional.empty();
  }

  private class VertexByUniqueId {
    private HashMap<String, HashMap<String, Vertex>> data = new HashMap<>();

    public boolean has(Collection collection, String identity) {
      return data.containsKey(collection.getCollectionName()) &&
        data.get(collection.getCollectionName()).containsKey(identity);
    }

    public Vertex get(Collection collection, String identity) {
      return data.get(collection.getCollectionName()).get(identity);
    }

    public void put(Collection collection, String uniqeValue, Vertex vertex) {
      if (!data.containsKey(collection.getCollectionName())) {
        data.put(collection.getCollectionName(), new HashMap<>());
      }
      data.get(collection.getCollectionName()).put(uniqeValue, vertex);
    }
  }
}
