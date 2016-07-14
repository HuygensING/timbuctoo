package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;

public class Collection {
  private final String vreName;
  private final Vertex vertex;
  private final CollectionMapper collectionMapper;

  public Collection(String vreName, Vertex vertex, CollectionMapper collectionMapper) {
    this.vreName = vreName;
    this.vertex = vertex;
    this.collectionMapper = collectionMapper;
  }

  public void add(Vertex entityVertex) {
    collectionMapper.addToCollection(entityVertex,
      new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName),
      vertex);
  }

  public String getVreName() {
    return vreName;
  }

  public CollectionDescription getDescription() {
    return new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName);
  }

  public Vertex getVertex() {
    return vertex;
  }
}
