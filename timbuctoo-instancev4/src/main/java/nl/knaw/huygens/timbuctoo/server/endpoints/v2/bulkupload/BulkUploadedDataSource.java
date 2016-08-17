package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulkUploadedDataSource implements DataSource {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadedDataSource.class);
  private final String vreName;
  private final String collectionName;
  private final GraphWrapper graphWrapper;
  private final TimbuctooErrorHandler errorHandler;
  private Map<String, Tuple<String, Map<Object, List<String>>>> cachedUris = new HashMap<>();

  public BulkUploadedDataSource(String vreName, String collectionName, GraphWrapper graphWrapper) {
    this.vreName = vreName;
    this.collectionName = collectionName;
    this.graphWrapper = graphWrapper;
    this.errorHandler = new TimbuctooErrorHandler(graphWrapper);
  }

  @Override
  public Iterator<Row> getRows(ErrorHandler defaultErrorHandler) {
    return graphWrapper.getGraph().traversal().V()
                       .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                       .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                       .out(TinkerpopSaver.RAW_COLLECTION_EDGE_NAME)
                       .has(TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName)
                       .out(TinkerpopSaver.RAW_ITEM_EDGE_NAME)
                       .toStream()
                       .map(vertex -> {
                         Map<String, Object> valueMap = new HashMap<>();
                         final Iterator<VertexProperty<Object>> properties = vertex.properties();
                         while (properties.hasNext()) {
                           VertexProperty prop = properties.next();
                           valueMap.put(prop.key(), prop.value());
                         }

                         for (Map.Entry<String, Tuple<String, Map<Object, List<String>>>> stringMapEntry : cachedUris
                           .entrySet()) {
                           final Tuple<String, Map<Object, List<String>>> stringMapTuple =
                             cachedUris.get(stringMapEntry.getKey());
                           List<String> uri = cachedUris.get(
                             stringMapEntry.getKey()).getRight().get(valueMap.get(stringMapTuple.getLeft())
                           );
                           valueMap.put(stringMapEntry.getKey(), uri);
                         }

                         errorHandler.setCurrentVertex(vertex);

                         return new Row(valueMap, errorHandler);
                       })
                       .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    if (referenceJoinValue != null) {
      cachedUris.computeIfAbsent(outputFieldName, x -> Tuple.tuple(fieldName, new HashMap<>()))
                .getRight()
                .computeIfAbsent(referenceJoinValue, x -> new ArrayList<>())
                .add(uri);
    }
  }

  private static class TimbuctooErrorHandler implements ErrorHandler {
    private final GraphWrapper graphWrapper;
    private Vertex currentVertex;

    public TimbuctooErrorHandler(GraphWrapper graphWrapper) {
      this.graphWrapper = graphWrapper;
    }

    @Override
    public void linkError(Map<String, Object> rowData, String childField, String parentCollection,
                          String parentField) {

      Object fieldValue = rowData.get(childField);
      if (fieldValue != null) {
        Graph graph = graphWrapper.getGraph();
        currentVertex.property(childField + "_error",
          String.format("'%s' does not exist in field '%s' of collection '%s'.",
          fieldValue,
          parentField,
          parentCollection
        ));
      }
    }

    public void setCurrentVertex(Vertex currentVertex) {
      this.currentVertex = currentVertex;
    }
  }
}
