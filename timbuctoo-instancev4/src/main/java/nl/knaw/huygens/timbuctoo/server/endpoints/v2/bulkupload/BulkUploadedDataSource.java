package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.ERROR_PREFIX;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.VALUE_PREFIX;

public class BulkUploadedDataSource implements DataSource {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadedDataSource.class);
  private final String vreName;
  private final String collectionName;
  private final GraphWrapper graphWrapper;
  private final TimbuctooErrorHandler errorHandler;

  private final JoinHandler joinHandler = new HashMapBasedJoinHandler();

  public static final String HAS_NEXT_ERROR = "hasNextError";

  public BulkUploadedDataSource(String vreName, String collectionName, GraphWrapper graphWrapper) {
    this.vreName = vreName;
    this.collectionName = collectionName;
    this.graphWrapper = graphWrapper;
    this.errorHandler = new TimbuctooErrorHandler(graphWrapper, vreName, collectionName);
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
                           if (prop.key().startsWith(VALUE_PREFIX)) {
                             valueMap.put(prop.key().substring(VALUE_PREFIX.length()), prop.value());
                           }
                           if (prop.key().equals("tim_id")) {
                             valueMap.put(prop.key(), prop.value());
                           }
                         }
                         // Adds
                         joinHandler.resolveReferences(valueMap);

                         errorHandler.setCurrentVertex(vertex);

                         return new Row(valueMap, errorHandler);
                       })
                       .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    if (referenceJoinValue != null) {
      joinHandler.willBeJoinedOn(fieldName, referenceJoinValue, uri, outputFieldName);
    }
  }

  private static class TimbuctooErrorHandler implements ErrorHandler {
    private final GraphWrapper graphWrapper;
    private final String vreName;
    private final String collectionName;
    private Vertex currentVertex;
    private Vertex lastError;


    public TimbuctooErrorHandler(GraphWrapper graphWrapper, String vreName, String collectionName) {
      this.graphWrapper = graphWrapper;
      this.vreName = vreName;
      this.collectionName = collectionName;
    }

    @Override
    public void linkError(Map<String, Object> rowData, String childField, String parentCollection,
                          String parentField) {

      Object fieldValue = rowData.get(childField);
      if (fieldValue != null) {
        Graph graph = graphWrapper.getGraph();
        currentVertex.property(ERROR_PREFIX + childField,
          String.format("'%s' does not exist in field '%s' of collection '%s'.",
          fieldValue,
          parentField,
          parentCollection
        ));
        if (lastError == null) {
          Vertex collection = graph.traversal().V()
                                   .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                                   .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                                   .out(TinkerpopSaver.RAW_COLLECTION_EDGE_NAME)
                                   .has(TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName)
                                   .next();
          collection.addEdge(HAS_NEXT_ERROR, currentVertex);
        } else {
          lastError.addEdge(HAS_NEXT_ERROR, currentVertex);
        }
        lastError = currentVertex;
      }
    }

    public void setCurrentVertex(Vertex currentVertex) {
      this.currentVertex = currentVertex;
    }
  }

  @Override
  public String toString() {
    return String.format("    BulkUploadedDatasource: %s, %s\n", this.vreName, this.collectionName);
  }
}
