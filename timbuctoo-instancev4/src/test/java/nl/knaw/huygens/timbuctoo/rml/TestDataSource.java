package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrLogicalSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.stream;

public class TestDataSource implements DataSource {
  private final Map<String, Iterable<Map<String, Object>>> data;
  private final Map<String, Map<String, Map<Object, String>>> cachedUris = new HashMap<>();

  public TestDataSource(Map<String, Iterable<Map<String, Object>>> data) {
    this.data = data;
  }

  @Override
  public Iterator<Map<String, Object>> getItems(RrLogicalSource rrLogicalSource, List<ReferenceGetter> references) {
    return stream(data.get(rrLogicalSource.getSource().getURI()))
      .map(values -> {
        ImmutableMap.Builder<String, Object> resultBuilder = ImmutableMap.<String, Object>builder().putAll(values);
        for (ReferenceGetter reference : references) {
          String uri = cachedUris.get(reference.source.getSource().getURI())
            .get(reference.targetFieldName)
            .get(values.get(reference.child));
          resultBuilder = resultBuilder.put(reference.referenceJoinFieldName, uri);
        }
        return (Map<String, Object>) resultBuilder.build();
      })
      .iterator();
  }

  @Override
  public void willBeJoinedOn(RrLogicalSource logicalSource, String fieldName, Object columnValue, String uri) {
    Map<Object, String> valueMap = cachedUris
      .computeIfAbsent(logicalSource.getSource().getURI(), x -> new HashMap<>())
      .computeIfAbsent(fieldName, x -> new HashMap<>());
    valueMap.put(columnValue, uri);
  }

}
