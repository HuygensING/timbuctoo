package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.ResultToC;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2017-06-07 10:24.
 */
public class BaseSerialization implements Serialization {

  private Deque<Entity> entityQueue = new ArrayDeque<>();
  private Deque<Edge> edgeQueue = new ArrayDeque<>();
  private int entityCount;
  private int edgeCount;

  private ResultToC resultToC;
  private TypeNameStore typeNameStore;


  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    entityCount = 0;
    edgeCount = 0;
    resultToC = tocGenerator.generateToC();
    this.typeNameStore = typeNameStore;
  }

  public ResultToC getResultToC() {
    return resultToC;
  }

  public TypeNameStore getTypeNameStore() {
    return typeNameStore;
  }

  public Map<String, ResultToC> getFlatToC() {
    Map<String, ResultToC> flatToc = new HashMap<>();
    collectFields(resultToC, flatToc);
    return flatToc;
  }

  private void collectFields(ResultToC resultToC, Map<String, ResultToC> flatToc) {
    LinkedHashMap<String, ResultToC> fields = resultToC.getFields();
    for (Map.Entry<String, ResultToC> entry : fields.entrySet()) {
      flatToc.put(entry.getKey(), entry.getValue());
      collectFields(entry.getValue(), flatToc);
    }
  }

  public List<String> getLeafFieldNames() {
    return getFlatToC().entrySet().stream()
                  .filter(e -> e.getValue().getFields().size() == 0)
                  .map(Map.Entry::getKey)
                  .collect(Collectors.toList());
  }

  @Override
  public void finish() throws IOException {

  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    //Entity entity = entityMap.computeIfAbsent(uri, Entity::new);
    Entity prevEntity = entityQueue.peek();
    Entity entity = new Entity(uri, entityCount++);
    entityQueue.addFirst(entity);
    Edge edge = edgeQueue.peek();
    if (edge != null) {
      edgeQueue.removeFirst();
      edge.setTargetEntity(entity);
      entity.addInEdge(edge);
      if (prevEntity != null) {
        prevEntity.addOutEdge(edge);
      }
      onEdge(edge);
    }
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    Edge edge = new Edge(propertyName, edgeCount++);
    Entity sourceEntity = entityQueue.peek();
    edge.setSourceEntity(sourceEntity);
    //sourceEntity.addOutEdge(edge);
    edgeQueue.addFirst(edge);
  }

  @Override
  public void onCloseEntity() throws IOException {
    onEntity(entityQueue.removeFirst());
  }

  @Override
  public void onStartList() throws IOException {
    edgeQueue.peek().setMultiple(true);
  }

  @Override
  public void onListItem(int index) {
    // clone previous edge and add to stack
    Edge edge = edgeQueue.peek();
    edgeQueue.addFirst(edge.copy(edgeCount++));
  }

  @Override
  public void onCloseList() throws IOException {
    // every list item gets a copy of edge so...
    edgeQueue.removeFirst();
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    // value is a SerializableValue.value
    Edge edge = edgeQueue.removeFirst();
    edge.setTarget(value);
    edge.setTargetType(valueType);
    entityQueue.peek().addOutEdge(edge);
    onEdge(edge);
  }

  @Override
  public void onValue(Object value) throws IOException {
    // value is a SerializableUntypedValue.value
    Edge edge = edgeQueue.removeFirst();
    edge.setTarget(value);
    entityQueue.peek().addOutEdge(edge);
    onEdge(edge);
  }

  public void onEdge(Edge edge) throws IOException {}

  public void onEntity(Entity entity) throws IOException {}

  public int getEntityCount() {
    return entityCount;
  }

  public int getEdgeCount() {
    return edgeCount;
  }

  public boolean isEntityQueueEmpty() {
    return entityQueue.isEmpty();
  }

  public boolean isEdgeQueueEmpty() {
    return edgeQueue.isEmpty();
  }

}
