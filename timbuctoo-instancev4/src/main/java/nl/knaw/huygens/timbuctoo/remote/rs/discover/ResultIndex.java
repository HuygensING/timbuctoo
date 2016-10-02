package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An index on explored URI's and their Result.
 */
public class ResultIndex {

  private Map<URI, Result<?>> resultMap = new HashMap<>();
  private Set<String> invalidUris = new TreeSet<>();
  private int count;

  public boolean contains(URI uri) {
    return resultMap.containsKey(uri);
  }

  public boolean contains(String uriString) {
    boolean present = false;
    try {
      URI uri = new URI(uriString);
      present = contains(uri);
    } catch (URISyntaxException e) {
      // cannot have been indexed as URI.
    }
    if (!present) {
      present = invalidUris.contains(uriString);
    }
    return present;
  }

  /**
   * Merge the given ResultIndex with this ResultIndex. If equal URI's are present the entry of this ResultIndex
   * has precedence, unless the Result of the given ResultIndex has content.
   * Invalidates the counter in this ResultMap: adding results or invalid URI-strings after this method not allowed.
   *
   * @param other ResultIndex to merge.
   * @return this in merged state.
   */
  public ResultIndex merge(ResultIndex other) {
    for (Map.Entry<URI, Result<?>> entry : other.resultMap.entrySet()) {
      if (!resultMap.containsKey(entry.getKey())) {
        resultMap.put(entry.getKey(), entry.getValue());
      } else if (entry.getValue().getContent().isPresent()) {
        resultMap.put(entry.getKey(), entry.getValue());
      }
    }
    invalidUris.addAll(other.invalidUris);
    count = -1;
    return this;
  }

  public Map<URI, Result<?>> getResultMap() {
    return resultMap;
  }

  public Set<String> getInvalidUris() {
    return invalidUris;
  }

  public int getCount() {
    return count;
  }

  void add(Result result) throws IllegalStateException {
    if (count == -1) {
      throw new IllegalStateException(
        "This " + getClass().getSimpleName() + " has been merged. Modification not allowed.");
    }
    result.setOrdinal(count++);
    resultMap.put(result.getUri(), result);
  }

  void addInvalidUri(String invalid) throws IllegalStateException {
    if (count == -1) {
      throw new IllegalStateException(
        "This " + getClass().getSimpleName() + " has been merged. Modification not allowed.");
    }
    count++;
    invalidUris.add(invalid);
  }

}
