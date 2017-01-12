package nl.knaw.huygens.timbuctoo.search;

import java.util.Map;

public class RawSearchResult {
  private long total;
  private Iterable<Map<String, Object>> results;

  public RawSearchResult() {

  }

  public RawSearchResult(long total, Iterable<Map<String, Object>> results) {
    this.total = total;
    this.results = results;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public Iterable<Map<String, Object>> getResults() {
    return results;
  }

  public void setResults(Iterable<Map<String, Object>> results) {
    this.results = results;
  }
}
