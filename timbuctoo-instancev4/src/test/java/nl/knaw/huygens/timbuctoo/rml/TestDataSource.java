package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.stream;

class TestDataSource implements DataSource {
  private final Iterable<Map<String, Object>> data;
  private final ErrorHandler errorHandler;
  private final JoinHandler joinHandler = new HashMapBasedJoinHandler();

  TestDataSource(Iterable<Map<String, Object>> data) {
    this(data, null);
  }

  TestDataSource(Iterable<Map<String, Object>> data, ErrorHandler errorHandler) {
    this.data = data;
    this.errorHandler = errorHandler;
  }

  @Override
  public Iterator<Row> getRows(ErrorHandler defaultErrorHandler) {
    return stream(data)
      .map(values -> {
        Map<String, Object> mutableValues = Maps.newHashMap(values);
        joinHandler.resolveReferences(mutableValues);
        Set<String> unMappedKeys = mutableValues
          .keySet().stream().filter(key -> mutableValues.get(key) == null).collect(Collectors.toSet());

        for (String key : unMappedKeys) {
          mutableValues.remove(key);
        }

        ImmutableMap.Builder<String, Object> resultBuilder = ImmutableMap.<String, Object>builder()
          .putAll(mutableValues);

        final ImmutableMap<String, Object> result = resultBuilder.build();
        return new Row(result, errorHandler == null ? defaultErrorHandler : errorHandler);
      })
      .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    joinHandler.announceJoinOn(fieldName, referenceJoinValue, uri, outputFieldName);
  }
}
