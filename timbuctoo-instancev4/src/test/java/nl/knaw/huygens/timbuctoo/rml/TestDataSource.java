package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.stream;

public class TestDataSource implements DataSource {
  private final Iterable<Map<String, Object>> data;
  private final Map<String, Tuple<String, Map<Object, String>>> cachedUris = new HashMap<>();

  public TestDataSource(Iterable<Map<String, Object>> data) {
    this.data = data;
  }

  @Override
  public Iterator<Map<String, Object>> getItems() {
    return stream(data)
      .map(values -> {
        ImmutableMap.Builder<String, Object> resultBuilder = ImmutableMap.<String, Object>builder().putAll(values);
        for (Map.Entry<String, Tuple<String, Map<Object, String>>> stringMapEntry : cachedUris.entrySet()) {
          final Tuple<String, Map<Object, String>> stringMapTuple = cachedUris.get(stringMapEntry.getKey());
          String uri = cachedUris.get(stringMapEntry.getKey()).getRight()
            .get(values.get(stringMapTuple.getLeft()));
          resultBuilder = resultBuilder.put(stringMapEntry.getKey(), uri);
        }

        return (Map<String, Object>) resultBuilder.build();
      })
      .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    Map<Object, String> valueMap = cachedUris
      .computeIfAbsent(outputFieldName, x -> Tuple.tuple(fieldName, new HashMap<>()))
      .getRight();
    valueMap.put(referenceJoinValue, uri);
  }

}
