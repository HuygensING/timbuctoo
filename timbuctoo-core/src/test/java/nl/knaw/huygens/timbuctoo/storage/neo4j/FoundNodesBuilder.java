package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FoundNodesBuilder {
  private Map<Label, Map<String, List<Node>>> foundNodes;

  public static FoundNodesBuilder noNodeIsFoundFor(Label label, String id) {
    return new FoundNodesBuilder(label, id);
  }

  public static FoundNodesBuilder foundNode(NodeMockBuilder node) {
    return new FoundNodesBuilder(node);
  }

  private FoundNodesBuilder(NodeMockBuilder node) {
    intialize();
    addNode(node);
  }

  private void intialize() {
    foundNodes = Maps.newHashMap();
  }

  private FoundNodesBuilder(Label label, String id) {
    intialize();
    addNewIdNodesMap(label, id);
  }

  private HashMap<String, List<Node>> addNewIdNodesMap(Label label, String id) {
    HashMap<String, List<Node>> idNodeMap = Maps.newHashMap();
    ArrayList<Node> newArrayList = Lists.newArrayList();
    idNodeMap.put(id, newArrayList);
    foundNodes.put(label, idNodeMap);

    return idNodeMap;
  }

  private void addNode(NodeMockBuilder node) {
    String id = "" + node.getId();
    for (Label label : node.getLabels()) {

      Map<String, List<Node>> idNodesMap = foundNodes.get(label);
      if (idNodesMap == null) {
        idNodesMap = addNewIdNodesMap(label, id);
      }

      List<Node> nodeSet = idNodesMap.get(id);
      if (nodeSet == null) {
        nodeSet = Lists.newArrayList();
        idNodesMap.put(id, nodeSet);
      }

      nodeSet.add(node.build());
    }
  }

  public FoundNodesBuilder andNode(NodeMockBuilder node) {
    addNode(node);
    return this;
  }

  public void inDB(GraphDatabaseService db) {
    for (Label label : foundNodes.keySet()) {
      Map<String, List<Node>> nodes = foundNodes.get(label);
      for (String id : nodes.keySet()) {
        foundInDB(db, label, id, nodes.get(id));
      }
    }
  }

  private void foundInDB(GraphDatabaseService db, Label label, String id, List<Node> nodes) {
    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(nodes.iterator());

    Iterable<Node> nodesIterable = IteratorUtil.asIterable(nodeIterator);
    ResourceIterable<Node> foundNodes = Iterables.asResourceIterable(nodesIterable);
    when(db.findNodesByLabelAndProperty(label, ID_PROPERTY_NAME, id)).thenReturn(foundNodes);
  }
}
