package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.CollectionsOfEntitiesSerialization;

public class GraphMlSerialization extends CollectionsOfEntitiesSerialization {

  private final PrintWriter writer;
  private int countEdge = 1;

  public GraphMlSerialization(OutputStream outputStream) throws IOException {
    writer = new PrintWriter(outputStream);
  }

  public void serialize(SerializableResult serializableResult) throws IOException {
    super.serialize(serializableResult);
    Map<String, Integer> entityIds = new HashMap<>();
    AtomicInteger nodeCounter = new AtomicInteger();
    writeHeader();
    for (Map<String, AggregatedEntity> entities : allEntities.values()) {
      for (Map.Entry<String, AggregatedEntity> entity : entities.entrySet()) {
        Integer nodeId = entityIds.computeIfAbsent(entity.getKey(), k -> nodeCounter.incrementAndGet());
        writer.println("    <node id=\"node" + nodeId + "\">");
        writer.println("      <data key=\"" + entity.getKey() + "\">" + entity.getValue() + "</data>");
        writer.println("      <data label=\"" + entity.getKey() + "\">" + entity.getValue() + "</data>");
        writer.println("    </node>");
        for (Map.Entry<String, Set<String>> relation : entity.getValue().relations.entrySet()) {
          for (String otherNodeUri : relation.getValue()) {
            Integer otherNodeId = entityIds.computeIfAbsent(otherNodeUri, k -> nodeCounter.incrementAndGet());
            writer.println("    <edge id=\"e" + countEdge  + "\" source=\"node" +
                nodeId  + "\" target=\"node" + otherNodeId + "\">");
            writer.println("      <data key=\"label\">" + relation.getKey() + "</data>");
            writer.println("    </edge>");
            countEdge++;
          }
        }
      }
    }
    writeFooter();
    writer.flush();
    writer.close();
  }
  

  protected void writeHeader() {
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +  
      "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" +
      "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
    writer.println(header);
    writer.println("  <graph id=\"G\" edgedefault=\"directed\">");
  }

  protected void writeFooter() {
    writer.println("  </graph>");
    String footer = "</graphml>";
    writer.println(footer);
  }

}
