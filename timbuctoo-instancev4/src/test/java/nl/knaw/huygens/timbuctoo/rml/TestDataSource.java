package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.stream;

class TestDataSource implements DataSource {
  private final Iterable<Map<String, String>> data;
  private final ErrorHandler errorHandler;
  private final JoinHandler joinHandler = new HashMapBasedJoinHandler();

  TestDataSource(Iterable<Map<String, String>> data) {
    this(data, null);
  }

  TestDataSource(Iterable<Map<String, String>> data, ErrorHandler errorHandler) {
    this.data = data;
    this.errorHandler = errorHandler;
  }

  @Override
  public Stream<Row> getRows(ErrorHandler defaultErrorHandler) {
    return stream(data)
      .map(values -> {
        Map<String, String> mutableValues =  new HashMap<>();
        Map<String, List<String>> joinValues = joinHandler.resolveReferences(mutableValues);

        Set<String> unMappedKeys = mutableValues.keySet().stream()
          .filter(key -> mutableValues.get(key) == null || mutableValues.get(key).isEmpty())
          .collect(Collectors.toSet());

        for (String key : unMappedKeys) {
          mutableValues.remove(key);
        }

        return (Row) new TestRow(mutableValues, joinValues, errorHandler == null ? defaultErrorHandler : errorHandler);
      });
  }

  @Override
  public void willBeJoinedOn(String fieldName, String referenceJoinValue, String uri, String outputFieldName) {
    joinHandler.willBeJoinedOn(fieldName, referenceJoinValue, uri, outputFieldName);
  }

  private class TestRow implements Row {

    private final Map<String, String> data;
    private final Map<String, List<String>> joinData;
    private final ErrorHandler errorHandler;

    public TestRow(Map<String, String> data, Map<String, List<String>> joinData, ErrorHandler errorHandler) {
      this.data = data;
      this.joinData = joinData;
      this.errorHandler = errorHandler;
    }

    @Override
    public String getRawValue(String key) {
      return data.get(key);
    }


    @Override
    public List<String> getJoinValue(String key) {
      return joinData.get(key);
    }

    @Override
    public void handleLinkError(String childField, String parentCollection, String parentField) {
      errorHandler.linkError(data, childField, parentCollection, parentField);
    }
  }
}
