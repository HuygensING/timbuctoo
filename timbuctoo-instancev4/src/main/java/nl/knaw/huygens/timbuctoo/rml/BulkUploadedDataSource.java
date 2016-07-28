package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.TimbuctooRawCollectionSource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulkUploadedDataSource implements DataSource {
  private final TimbuctooRawCollectionSource source;
  private final GraphWrapper graphWrapper;
  private Map<String, Tuple<String, Map<Object, List<String>>>> cachedUris = new HashMap<>();

  public BulkUploadedDataSource(TimbuctooRawCollectionSource source, GraphWrapper graphWrapper) {
    this.source = source;
    this.graphWrapper = graphWrapper;
  }

  @Override
  public Iterator<Map<String, Object>> getItems() {
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

        for (Map.Entry<String, Tuple<String, Map<Object, List<String>>>> stringMapEntry : cachedUris.entrySet()) {
          final Tuple<String, Map<Object, List<String>>> stringMapTuple = cachedUris.get(stringMapEntry.getKey());
          List<String> uri = cachedUris.get(
            stringMapEntry.getKey()).getRight().get(valueMap.get(stringMapTuple.getLeft())
          );
          valueMap.put(stringMapEntry.getKey(), uri);
        }

        return valueMap;
      })
      .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    if (referenceJoinValue != null) {
      cachedUris
        .computeIfAbsent(outputFieldName, x -> Tuple.tuple(fieldName, new HashMap<>()))
        .getRight()
        .computeIfAbsent(referenceJoinValue, x -> new ArrayList<>())
        .add(uri);
    }
  }
}
