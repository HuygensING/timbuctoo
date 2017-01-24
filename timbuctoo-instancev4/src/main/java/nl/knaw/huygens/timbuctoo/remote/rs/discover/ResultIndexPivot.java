package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.SitemapItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Sitemapindex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.UrlItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data summarizations on a ResultIndex.
 */
public class ResultIndexPivot {

  private ResultIndex resultIndex;

  public ResultIndexPivot(ResultIndex resultIndex) {
    this.resultIndex = resultIndex;
  }

  public List<Throwable> listErrors() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> !result.getErrors().isEmpty())
      .flatMap(result -> result.getErrors().stream())
      .collect(Collectors.toList());
  }

  public List<Result<?>> listErrorResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> !result.getErrors().isEmpty())
      .collect(Collectors.toList());
  }

  public List<Result<?>> listResultsWithContent() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent())
      .collect(Collectors.toList());
  }

  public List<Result<?>> listFirstResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getOrdinal() == 0)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Urlset>> listUrlsetResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Urlset>> listUrlsetResults(Capability capability) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Urlset>> listUrlsetResultsByLevel(int capabilityLevel) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getLevel).orElse(-1) == capabilityLevel)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Sitemapindex>> listSitemapindexResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Sitemapindex>> listSitemapindexResults(Capability capability) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<Sitemapindex>> listSitemapindexResultsByLevel(int capabilityLevel) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getLevel).orElse(-1) == capabilityLevel)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<RsRoot>> listRsRootResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof RsRoot)
      .map(result -> (Result<RsRoot>) result)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<RsRoot>> listRsRootResults(Capability capability) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof RsRoot)
      .map(result -> (Result<RsRoot>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("Invalid capability").equals(capability.xmlValue))
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<RsRoot>> listRsRootResultsByLevel(int capabilityLevel) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof RsRoot)
      .map(result -> (Result<RsRoot>) result)
      .filter(result -> result.getContent()
        .map(RsRoot::getLevel).orElse(-1) == capabilityLevel)
      .collect(Collectors.toList());
  }

  @SuppressWarnings ("unchecked")
  public List<Result<LinkList>> listLinkListResults() {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof LinkList)
      .map(result -> (Result<LinkList>) result)
      .collect(Collectors.toList());
  }

  /**
   * List the values of the &lt;loc&gt; element of &lt;url&gt; elements of documents of type urlset with
   * the given capability.
   * @param capability the capability of the documents from which locations will be extracted
   * @return List of values of the &lt;loc&gt; elements
   */
  @SuppressWarnings ("unchecked")
  public List<String> listUrlLocations(Capability capability) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Urlset)
      .map(result -> (Result<Urlset>) result)
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .map(urlsetResult -> urlsetResult.getContent().orElse(null))
      .map(Urlset::getItemList)
      .flatMap(List::stream)
      .map(UrlItem::getLoc)
      .collect(Collectors.toList());
  }

  /**
   * List the values of the &lt;loc&gt; element of &lt;sitemap&gt; elements of documents of type sitemapindex with
   * the given capability.
   * @param capability the capability of the documents from which locations will be extracted
   * @return List of values of the &lt;loc&gt; elements
   */
  @SuppressWarnings ("unchecked")
  public List<String> listSitemapLocations(Capability capability) {
    return resultIndex.getResultMap().values().stream()
      .filter(result -> result.getContent().isPresent() && result.getContent().orElse(null) instanceof Sitemapindex)
      .map(result -> (Result<Sitemapindex>) result)
      .filter(result -> result.getContent().map(RsRoot::getMetadata)
        .flatMap(RsMd::getCapability).orElse("invalid").equals(capability.xmlValue))
      .map(sitemapindexResult -> sitemapindexResult.getContent().orElse(null))
      .map(Sitemapindex::getItemList)
      .flatMap(List::stream)
      .map(SitemapItem::getLoc)
      .collect(Collectors.toList());
  }
}
