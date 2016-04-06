package nl.knaw.huygens.timbuctoo.graph;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class D3Graph {
  public static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

  private final List<Node> nodes;
  private final List<Link> links;

  public D3Graph() {
    nodes = Lists.newArrayList();
    links = Lists.newArrayList();
  }

  public void addNode(Vertex vertex) {
    try {
      Node node = new Node(vertex);

      if (!nodes.contains(node)) {
        nodes.add(node);
      }

    } catch (IOException e) {
      LOG.warn("Node not added because " +  e.getMessage());
    }
  }

  public void addLink(Edge edge, Vertex source, Vertex target) {
    try {
      Link link = new Link(edge, nodes.indexOf(new Node(source)), nodes.indexOf(new Node(target)));
      if (!links.contains(link)) {
        links.add(link);
      }
    } catch (IOException e) {
      LOG.warn("Link not added because " + e.getMessage());
    }
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<Link> getLinks() {
    return links;
  }

  @Override
  public String toString() {
    return "D3Graph{" +
            "nodes=" + nodes +
            ", links=" + links +
            '}';
  }
}
