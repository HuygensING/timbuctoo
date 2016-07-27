package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrLogicalSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDataSource implements DataSource{
  private final Map<String, Iterable<Map<String, Object>>> data;
  private final Map<String, Map<String, Map<Object, String>>> cachedUris = new HashMap<>();

  public TestDataSource(Map<String, Iterable<Map<String, Object>>> data) {
    this.data = data;
  }

  @Override
  public Iterable<Map<String, Object>> getItems(RrLogicalSource rrLogicalSource, List<ReferenceGetter> references) {
    final Iterable<Map<String, Object>> valuesForThisSource = data.get(rrLogicalSource.source.getURI());
    for (Map<String, Object> values : valuesForThisSource) {
      for (ReferenceGetter reference : references) {
        String uri = cachedUris.get(reference.source.source.getURI()).get(reference.targetFieldName).get(values.get(reference.child));
        values.put(reference.referenceJoinFieldName, uri);
      }
    }

    return valuesForThisSource;
  }

  @Override
  public void willBeJoinedOn(RrLogicalSource logicalSource, String fieldName, Object columnValue, String uri) {
    Map<Object, String> valueMap = cachedUris
      .computeIfAbsent(logicalSource.source.getURI(), x -> new HashMap<>())
      .computeIfAbsent(fieldName, x -> new HashMap<>());
    valueMap.put(columnValue, uri);
  }

}
