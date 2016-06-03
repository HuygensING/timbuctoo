package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.EdgeExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.GraphReadUtils;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
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

class EdgePropertyGetter {

  private final Collection relationCollection;
  private final String vreId;
  private final Vres mappings;
  private final String[] edgeLabels;

  EdgePropertyGetter(String vreId, Collection relationCollection, Vres mappings, String[] edgeLabels) {
    this.vreId = vreId;
    this.relationCollection = relationCollection;
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

        // FIXME: string concatenating methods like this should be delegated to a configuration class
        Optional<Boolean> isAccepted =
          GraphReadUtils
            .getProp(edge, relationCollection.getEntityTypeName() + "_accepted", Boolean.class);

        // Only add the accepted relations
        if (isAccepted.isPresent() && isAccepted.get()) {
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

      // Add propertyColDescription and excelDescription like above
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
