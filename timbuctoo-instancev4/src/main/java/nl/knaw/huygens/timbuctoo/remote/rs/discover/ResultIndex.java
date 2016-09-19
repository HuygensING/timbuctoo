package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Sitemapindex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ResultIndex {

  private Map<URI, Result<?>> resultMap = new HashMap<>();
  private Set<String> invalidUris = new TreeSet<>();

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

  public Map<URI, Result<?>> getResultMap() {
    return resultMap;
  }

  public Set<String> getInvalidUris() {
    return invalidUris;
  }

  public List<Throwable> getErrors() {
    return resultMap.values().stream()
      .filter(result -> result.getError().isPresent())
      .map(result -> result.getError().orElse(null))
      .collect(Collectors.toList());
  }

  public List<Result<?>> getErrorResults() {
    return resultMap.values().stream()
      .filter(result -> result.getError().isPresent())
      .collect(Collectors.toList());
  }

  public List<Result<?>> getResults() {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent())
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Urlset>> getUrlsetResults() {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Urlset>> getUrlsetResults(Capability capability) {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Sitemapindex>> getSitemapindexResults() {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Sitemapindex>> getSitemapindexResults(Capability capability) {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<RsRoot>> getRsRootResults() {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof RsRoot)
      .map(result -> (Result<RsRoot>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<RsRoot>> getRsRootResults(Capability capability) {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof RsRoot)
      .map(result -> (Result<RsRoot>) result)
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("Invalid capability").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<LinkList>> getLinkListResults() {
    return resultMap.values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof LinkList)
      .map(result -> (Result<LinkList>) result)
      .collect(Collectors.toList());
  }

  void add(Result result) {
    resultMap.put(result.getUri(), result);
  }

  void addInvalidUri(String invalid) {
    invalidUris.add(invalid);
  }

}
