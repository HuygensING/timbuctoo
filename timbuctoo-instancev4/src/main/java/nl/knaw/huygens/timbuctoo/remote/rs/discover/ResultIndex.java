package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ResultIndex {

  private Map<URI, Result> resultMap = new HashMap<>();
  private Set<String> invalidUris = new TreeSet<>();

  public void add(Result result) {
    resultMap.put(result.getUri(), result);
  }

  public void addInvalidUri(String invalid) {
    invalidUris.add(invalid);
  }

  public boolean contains(URI uri) {
    return resultMap.containsKey(uri);
  }

  public boolean contains(String uriString) {
    boolean present = false;
    try {
      URI uri = new URI(uriString);
      present = contains(uri);
    } catch (URISyntaxException e) {
      // cannot be indexed as URI.
    }
    if (!present) {
      present = invalidUris.contains(uriString);
    }
    return present;
  }
}
