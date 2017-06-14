package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.ResultToC;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
  private String previousField;
  //private boolean listEntity;

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    //System.out.println("initialize " + tocGenerator + " " + typeNameStore);
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
    Map<String, ResultToC> flatToc = new LinkedHashMap<>();
    collectFlatToC(resultToC, flatToc);
    return flatToc;
  }

  private void collectFlatToC(ResultToC resultToC, Map<String, ResultToC> flatToc) {
    LinkedHashMap<String, ResultToC> fields = resultToC.getFields();
    for (Map.Entry<String, ResultToC> entry : fields.entrySet()) {
      flatToc.put(entry.getKey(), entry.getValue());
      collectFlatToC(entry.getValue(), flatToc);
    }
  }

  public List<String> getLeafFieldNames() {
    // return getFlatToC().entrySet().stream()
    //               .filter(e -> e.getValue().getFields().size() == 0)
    //               .map(Map.Entry::getKey)
    //               .collect(Collectors.toList());
    Set<String> leafFields = new TreeSet<>();
    collectLeafFields(resultToC, leafFields);
    return new ArrayList<>(leafFields);
  }

  private void collectLeafFields(ResultToC resultToC, Set<String> leafFields) {
    LinkedHashMap<String, ResultToC> fields = resultToC.getFields();
    for (Map.Entry<String, ResultToC> entry : fields.entrySet()) {
      //System.out.println(entry.getKey() + " " + entry.getValue().getFields().size());
      if ("items".equals(entry.getKey())) {
        leafFields.add(previousField);
      } else if (entry.getValue().getFields().size() == 0) {
        leafFields.add(entry.getKey());
      }
      previousField = entry.getKey();
      collectLeafFields(entry.getValue(), leafFields);
    }
  }

  @Override
  public void finish() throws IOException {
    //System.out.println("finish");
  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    //System.out.println("onStartEntity "+ entityCount + " " + uri + (uri == null ? " <<<<<<<<<<<<<<<<<<<<<<<" : ""));
    if (uri == null) { // wrapper object around list
      return;
    }
    Edge edge = edgeQueue.peek();

    Entity sourceEntity = entityQueue.peek();
    Entity targetEntity = new Entity(uri, entityCount++);
    entityQueue.addFirst(targetEntity);

    if (edge != null) {
      edgeQueue.removeFirst();
      edge.setTargetEntity(targetEntity);
      targetEntity.addInEdge(edge);
      if (sourceEntity != null) {
        sourceEntity.addOutEdge(edge);
      }
      onEdge(edge);
    }
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    //System.out.println("onProperty " + propertyName);
    if ("items".equals(propertyName)) { // list items: ignore this propertyName
      return;
    }
    Edge edge = new Edge(propertyName, edgeCount++);
    Entity sourceEntity = entityQueue.peek();
    edge.setSourceEntity(sourceEntity);
    edgeQueue.addFirst(edge);
  }

  @Override
  public void onCloseEntity(String uri) throws IOException {
    //System.out.println("onCloseEntity");
    if (uri == null) { // ignored wrapper object around list
      return;
    }
    Entity entity = entityQueue.peek();
    if (entity != null) {
      onEntity(entityQueue.removeFirst());
    }
  }

  @Override
  public void onStartList() throws IOException {
    //System.out.println("onStartList");
  }

  @Override
  public void onListItem(int index) {
    //System.out.println("onListItem " + index);
    // clone previous edge and add to stack
    Edge edge = edgeQueue.peek();
    edgeQueue.addFirst(edge.copy(edgeCount++));
  }

  @Override
  public void onCloseList() throws IOException {
    //System.out.println("onCloseList");
    // every list item gets a copy of edge so...
    edgeQueue.removeFirst();
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    //System.out.println("onRdfValue " + value + " " + valueType);
    // value is a SerializableValue.value
    Edge edge = edgeQueue.removeFirst();
    edge.setTarget(value);
    edge.setTargetType(valueType);
    entityQueue.peek().addOutEdge(edge);
    onEdge(edge);
  }

  @Override
  public void onValue(Object value) throws IOException {
    //System.out.println("onValue " + value);
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
