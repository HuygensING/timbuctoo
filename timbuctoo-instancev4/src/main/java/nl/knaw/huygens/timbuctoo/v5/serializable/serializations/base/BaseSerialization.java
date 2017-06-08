package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.ResultToC;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2017-06-07 10:24.
 */
public class BaseSerialization implements Serialization {

  private Deque<Entity> entities = new ArrayDeque<>();
  private Deque<Edge> edges = new ArrayDeque<>();


  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    ResultToC resultToC = tocGenerator.generateToC();
    Set<String> flatToc = new HashSet<>();
    collectFields(resultToC, flatToc);
    onFlatToc(flatToc, typeNameStore);
  }

  private void collectFields(ResultToC resultToC, Set<String> flatToc) {
    LinkedHashMap<String, ResultToC> fields = resultToC.getFields();
    for (Map.Entry<String, ResultToC> entry : fields.entrySet()) {
      flatToc.add(entry.getKey());
      collectFields(entry.getValue(), flatToc);
    }
  }

  @Override
  public void finish() throws IOException {
    //System.out.println("finish " + entities + " " + edges);
  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    //Entity entity = entityMap.computeIfAbsent(uri, Entity::new);
    Entity entity = new Entity(uri);
    entities.addFirst(entity);
    Edge edge = edges.peek();
    if (edge != null) {
      edges.removeFirst();
      edge.setTargetEntity(entity);
      entity.addInEdge(edge);
      onEdge(edge);
    }
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    Edge edge = new Edge(propertyName);
    Entity sourceEntity = entities.peek();
    edge.setSourceEntity(sourceEntity);
    sourceEntity.addOutEdge(edge);
    edges.addFirst(edge);
  }

  @Override
  public void onCloseEntity() throws IOException {
    onEntity(entities.removeFirst());
  }

  @Override
  public void onStartList() throws IOException {
  }

  @Override
  public void onListItem(int index) {
    // clone previous edge and add to stack
    Edge edge = edges.peek();
    edges.addFirst(edge.copy());
  }

  @Override
  public void onCloseList() throws IOException {
    // every list item gets a copy of edge so...
    edges.removeFirst();
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    // value is a SerializableValue.value
    Edge edge = edges.removeFirst();
    edge.setTarget(value);
    edge.setTargetType(valueType);
    onEdge(edge);
  }

  @Override
  public void onValue(Object value) throws IOException {
    // value is a SerializableUntypedValue.value
    Edge edge = edges.removeFirst();
    edge.setTarget(value);
    onEdge(edge);
  }

  public void onFlatToc(Set<String> flatToc, TypeNameStore typeNameStore) throws IOException {
    //flatToc.forEach(System.out::println);
  }

  public void onEdge(Edge edge) throws IOException {
    //System.out.println(edge);
  }

  public void onEntity(Entity entity) throws IOException {
    //System.out.println(entity);
  }
}
