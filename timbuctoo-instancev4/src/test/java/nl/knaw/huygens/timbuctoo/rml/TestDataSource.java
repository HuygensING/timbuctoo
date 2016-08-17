package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.stream;

public class TestDataSource implements DataSource {
  private final Iterable<Map<String, Object>> data;
  private final ErrorHandler errorHandler;
  private final Map<String, Tuple<String, Map<Object, String>>> cachedUris = new HashMap<>();

  public TestDataSource(Iterable<Map<String, Object>> data) {
    this(data, null);
  }

  public TestDataSource(Iterable<Map<String, Object>> data, ErrorHandler errorHandler) {
    this.data = data;
    this.errorHandler = errorHandler;
  }

  @Override
  public Iterator<Row> getRows(ErrorHandler defaultErrorHandler) {
    return stream(data)
      .map(values -> {
        ImmutableMap.Builder<String, Object> resultBuilder = ImmutableMap.<String, Object>builder().putAll(values);
        resultBuilder = mapRefs(values, resultBuilder);

        return new Row(resultBuilder.build(), errorHandler == null ? defaultErrorHandler : errorHandler);
      })
      .iterator();
  }

  private ImmutableMap.Builder<String, Object> mapRefs(Map<String, Object> values,
                                                       ImmutableMap.Builder<String, Object> resultBuilder) {
    for (Map.Entry<String, Tuple<String, Map<Object, String>>> cachedUri : cachedUris.entrySet()) {
      final Tuple<String, Map<Object, String>> stringMapTuple = cachedUris.get(cachedUri.getKey());
      String uri = cachedUris.get(cachedUri.getKey()).getRight()
                             .get(values.get(stringMapTuple.getLeft()));
      if (uri != null) {
        resultBuilder = resultBuilder.put(cachedUri.getKey(), uri);
      }
    }
    return resultBuilder;
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    Map<Object, String> joinMap = cachedUris
      .computeIfAbsent(outputFieldName, x -> Tuple.tuple(fieldName, new HashMap<>()))
      .getRight();
    joinMap.put(referenceJoinValue, uri);
  }

}
