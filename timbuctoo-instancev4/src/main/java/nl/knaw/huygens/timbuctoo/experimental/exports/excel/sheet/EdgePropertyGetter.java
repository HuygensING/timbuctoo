package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.EdgeExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

class EdgePropertyGetter {

  private final String relationAcceptedPropertyName;
  private final String vreId;
  private final Vres mappings;
  private final String[] edgeLabels;

  EdgePropertyGetter(String vreId, String relationAcceptedPropertyName, Vres mappings, String[] edgeLabels) {
    this.vreId = vreId;
    this.relationAcceptedPropertyName = relationAcceptedPropertyName;
    this.mappings = mappings;
    this.edgeLabels = edgeLabels;
  }

  GraphTraversal<Vertex, Vertex> getEdgeExcelDataTraversal(PropertyColumnMetadata propertyColumnMetadata,
                                                           PropertyData propertyData) {
    return __.<Vertex>sideEffect(x -> {
      // Map of edges per type (to arrange the cols correctly)
      Map<String, List<Edge>> edgeMap = Maps.newHashMap();
      Iterator<Edge> edgeIterator = edgeLabels == null ?
        x.get().edges(Direction.OUT) :
        x.get().edges(Direction.OUT, edgeLabels);

      edgeIterator.forEachRemaining(edge -> {

        Optional<Boolean> isLatest = getProp(edge, "isLatest", Boolean.class);
        Optional<Boolean> isAccepted = getProp(edge, relationAcceptedPropertyName, Boolean.class);

        // Only add the accepted relations
        if (isAccepted.isPresent() && isAccepted.get() && isLatest.isPresent() && isLatest.get()) {
          // Initialize new set if this edge type is not yet in the map and retrieve the set of edges
          // for this edge type
          List<Edge> edges = edgeMap.containsKey(edge.label()) ? edgeMap.get(edge.label()) : Lists.newArrayList();
          if (!edgeMap.containsKey(edge.label())) {
            edgeMap.put(edge.label(), edges);
          }
          // Add this edge to the current set
          edges.add(edge);
        }
      });

      // Add column metadata and data from edges in map in holder objects (PropertyColumnMetadata, PropertyData)
      for (Map.Entry<String, List<Edge>> entry : edgeMap.entrySet()) {
        // Make one ExcelDescription for all the edges of this type
        ExcelDescription edgeDescription =
          new EdgeExcelDescription(entry.getValue(), mappings, vreId);
        // Add columnmetadata for edges of this type
        propertyColumnMetadata.addColumnInformation(edgeDescription, entry.getKey());
        // Add this excelDescription to the excelDescriptions
        propertyData.putProperty(entry.getKey(), edgeDescription);
      }
    });
  }
}
