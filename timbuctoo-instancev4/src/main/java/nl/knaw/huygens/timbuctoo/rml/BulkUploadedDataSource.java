package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.TimbuctooRawCollectionSource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BulkUploadedDataSource implements DataSource {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadedDataSource.class);
  private final TimbuctooRawCollectionSource source;
  private final GraphWrapper graphWrapper;
  private final ErrorHandler errorHandler;
  private Map<String, Tuple<String, Map<Object, List<String>>>> cachedUris = new HashMap<>();

  public BulkUploadedDataSource(TimbuctooRawCollectionSource source, GraphWrapper graphWrapper) {
    this.source = source;
    this.graphWrapper = graphWrapper;
    errorHandler = new TimbuctooErrorHandler(graphWrapper);
  }

  @Override
  public Iterator<Row> getRows() {
    return graphWrapper.getGraph().traversal().V()
                       .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                       .has(Vre.VRE_NAME_PROPERTY_NAME, source.getVreName())
                       .out(TinkerpopSaver.RAW_COLLECTION_EDGE_NAME)
                       .has(TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME, source.getRawCollectionName())
                       .out(TinkerpopSaver.RAW_ITEM_EDGE_NAME)
                       .valueMap()
                       .toStream()
                       .map(valueMap -> {
                         for (String key : valueMap.keySet()) {
                           valueMap.put(key, ((List) valueMap.get(key)).get(0));
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

    public TimbuctooErrorHandler(GraphWrapper graphWrapper) {
      this.graphWrapper = graphWrapper;
    }

    @Override
    public void handleLink(Map<String, Object> rowData, String childField, String parentCollection,
                           String parentField) {

      Object fieldValue = rowData.get(childField);
      if (fieldValue != null) {
        Object timId = rowData.get("tim_id");
        Graph graph = graphWrapper.getGraph();
        Optional<Vertex> vertexOptional = graph.traversal().V().has("tim_id", timId).tryNext();
        if (vertexOptional.isPresent()) {
          Vertex vertex = vertexOptional.get();
          vertex.property(childField + "_error", String.format("'%s' does not exist in field '%s' of collection '%s'.",
            fieldValue,
            parentField,
            parentCollection
          ));
          graph.tx().commit();
        } else {
          LOG.error("Trying to log mapping error on vertex, but vertex with id '{}' does not exist.", timId);
        }
      }
    }
  }
}
