package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsBuilder;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Sitemapindex;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Download ResourceSync Framework documents.
 * <p>
 *   RsExplorer can be used to selectively explore the hierarchy of ResourceSync sitemaps at a remote site.
 *   In addition to this, it also verifies the validity of the various links between these sitemaps.
 *   The result of each request to an individual URI is gathered in a {@link Result}; all results are
 *   gathered in a {@link ResultIndex}.
 * </p>
 * <p>
 *   RsExplorer can begin with any URI that points to a ResourceSync sitemap on the site
 *   that is the subject of exploration, regardless of what capability that start document may have. With default
 *   settings RsExplorer will navigate and index the complete tree of documents.
 *   There are three switches to influence the path the RsExplorer will take through the sitemap tree:
 *   {@link #followParentLinks}, {@link #followChildLinks} and {@link #followIndexLinks}.
 * </p>
 */
public class RsExplorer extends AbstractUriExplorer {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RsExplorer.class);

  private final ResourceSyncContext rsContext;

  private LambdaExceptionUtil.Function_WithExceptions<HttpResponse, RsRoot, Exception> sitemapConverter;

  public boolean followParentLinks = true;
  public boolean followIndexLinks = true;
  public boolean followChildLinks = true;
  public boolean followDescribedByLinks = true;

  public RsExplorer(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    super(httpClient);
    this.rsContext = rsContext;
  }

  /**
   * Follow links in child element &lt;rs:ln&gt; of &lt;urlset&gt;
   * with the relation type <code>up</code>. Default is <code>true</code>. Set to <code>false</code>
   * if you want to prevent following links to parent documents.
   *
   * @param follow <code>true</code> if following parent links, <code>false</code> otherwise.
   * @return <code>this</code> to enable method chaining
   */
  public RsExplorer withFollowParentLinks(boolean follow) {
    followParentLinks = follow;
    return this;
  }

  public RsExplorer withFollowIndexLinks(boolean follow) {
    followIndexLinks = follow;
    return this;
  }

  public RsExplorer withFollowChildLinks(boolean follow) {
    followChildLinks = follow;
    return this;
  }

  public RsExplorer withFollowDescribedByLinks(boolean follow) {
    followDescribedByLinks = follow;
    return this;
  }

  public RsExplorer withSitemapConverter(
    LambdaExceptionUtil.Function_WithExceptions<HttpResponse, RsRoot, Exception> converter) {
    this.sitemapConverter = converter;
    return this;
  }

  public ResultIndex explore(URI uri) {
    ResultIndex index = new ResultIndex();
    explore(uri, index);
    return index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Result<RsRoot> explore(URI uri, ResultIndex index) {
    //System.out.println("Exploring URI " + uri);
    LOG.debug("Exploring URI " + uri);
    Result<RsRoot> result = execute(uri, getSitemapConverter());
    index.add(result);
    Capability capability = extractCapability(result);

    if (followParentLinks) {
      // rs:ln rel="up" -> points to parent document, a urlset.
      String parentLink = result.getContent().map(rsRoot -> rsRoot.getLink("up")).orElse(null);
      if (parentLink != null && !index.contains(parentLink)) {
        try {
          URI parentUri = new URI(parentLink);
          Result<RsRoot> parentResult = explore(parentUri, index);
          result.addParent(parentResult);
          verifyUpRelation(result, parentResult, capability);
        } catch (URISyntaxException e) {
          index.addInvalidUri(parentLink);
          result.addError(e);
          result.addInvalidUri(parentLink);
        }
      }
    }

    if (followIndexLinks) {
      // rs:ln rel="index" -> points to parent index, a sitemapindex.
      String indexLink = result.getContent().map(rsRoot -> rsRoot.getLink("index")).orElse(null);
      if (indexLink != null && !index.contains(indexLink)) {
        try {
          URI indexUri = new URI(indexLink);
          Result<RsRoot> indexResult = explore(indexUri, index);
          result.addParent(indexResult);
          verifyIndexRelation(result, indexResult, capability);
        } catch (URISyntaxException e) {
          index.addInvalidUri(indexLink);
          result.addError(e);
          result.addInvalidUri(indexLink);
        }
      }
    }

    if (followChildLinks) {
      // elements <url> or <sitemap> have the location of the children of result.
      // children of Urlset with capability resourcelist, resourcedump, changelist, changedump
      // are the resources them selves. do not explore these with this explorer.
      String xmlString = result.getContent()
                               .map(RsRoot::getMetadata).flatMap(RsMd::getCapability).orElse("invalid");

      boolean isSitemapindex = result.getContent().map(rsRoot -> rsRoot instanceof Sitemapindex).orElse(false);

      if (Capability.levelfor(xmlString) > Capability.RESOURCELIST.level || isSitemapindex) {
        List<RsItem> itemList = result.getContent().map(RsRoot::getItemList).orElse(Collections.emptyList());
        for (RsItem item : itemList) {
          String childLink = item.getLoc();
          if (childLink != null && !index.contains(childLink)) {
            try {
              URI childUri = new URI(childLink);
              Result<RsRoot> childResult = explore(childUri, index);
              result.addChild(childResult);
              verifyChildRelation(result, childResult, capability);
              loadDescriptionIfApplicable(childResult, item.getLink("describedby"), index);
            } catch (URISyntaxException e) {
              index.addInvalidUri(childLink);
              result.addError(e);
              result.addInvalidUri(childLink);
            }
          }
        }
      }
    }

    // rs:ln rel="describedby" -> points to a document that describes the result that is returned by this method.
    String describedByLink = result.getContent().map(rsRoot -> rsRoot.getLink("describedby")).orElse(null);
    loadDescriptionIfApplicable(result, describedByLink, index);

    return result;
  }

  private void loadDescriptionIfApplicable(Result<RsRoot> parent, String describedByUrl, ResultIndex index) {
    if (followDescribedByLinks && describedByUrl != null && !index.contains(describedByUrl)) {
      //System.out.println("Following describedBy link " + describedByUrl);
      LOG.debug("Following describedBy link " + describedByUrl);
      try {
        URI describedByUri = new URI(describedByUrl);
        Result<Description> describedByResult = execute(describedByUri, descriptionReader);
        parent.setDescriptionResult(describedByResult);
        index.add(describedByResult);
      } catch (URISyntaxException e) {
        index.addInvalidUri(describedByUrl);
        parent.addError(e);
        parent.addInvalidUri(describedByUrl);
      }
    }
  }

  private Capability extractCapability(Result<RsRoot> result) {
    String xmlString = result.getContent()
                             .map(RsRoot::getMetadata).flatMap(RsMd::getCapability).orElse("");
    Capability capa = null;
    try {
      capa = Capability.forString(xmlString);
    } catch (IllegalArgumentException e) {
      result.addError(new RemoteResourceSyncFrameworkException(
        String.format("invalid value for capability: '%s'", xmlString)));
    }
    return capa;
  }

  private void verifyUpRelation(Result<RsRoot> result, Result<RsRoot> parentResult, Capability capability) {
    if (result.getContent().isPresent() && parentResult.getContent().isPresent()) {
      Capability parentCapa = extractCapability(parentResult);

      if (capability != null && !capability.verifyUpRelation(parentCapa)) {
        result.addError(new RemoteResourceSyncFrameworkException(
          String.format("invalid up relation: Expected '%s', found '%s'",
            capability.getUpRelation() == null ? "<no relation>" : capability.getUpRelation().xmlValue,
            parentCapa == null ? "<no relation>" : parentCapa.xmlValue)));
      }
    }

    // up relation is always to a urlset
    if (!parentResult.getContent().map(content -> content instanceof Urlset).orElse(true)) {
      result.addError(new RemoteResourceSyncFrameworkException(
        "invalid up relation: parent document is not '<urlset>'"));
    }

  }

  private void verifyIndexRelation(Result<RsRoot> result, Result<RsRoot> parentResult, Capability capability) {
    if (result.getContent().isPresent() && parentResult.getContent().isPresent()) {
      Capability parentCapa = extractCapability(parentResult);

      if (capability != null && !capability.verifyIndexRelation(parentCapa)) {
        result.addError(new RemoteResourceSyncFrameworkException(
          String.format("invalid index relation: Expected '%s', found '%s'",
            capability.getIndexRelation() == null ? "<no relation>" : capability.getIndexRelation().xmlValue,
            parentCapa == null ? "<no relation>" : parentCapa.xmlValue)));
      }
    }

    // index relation is always to a sitemapindex
    if (!parentResult.getContent().map(content -> content instanceof Sitemapindex).orElse(true)) {
      result.addError(new RemoteResourceSyncFrameworkException(
        "invalid index relation: parent document is not '<sitemapindex>'"));
    }
  }

  private void verifyChildRelation(Result<RsRoot> result, Result<RsRoot> childResult, Capability capability) {
    if (result.getContent().isPresent() && childResult.getContent().isPresent()) {
      Capability childCapa = extractCapability(childResult);

      if (capability != null && !capability.verifyChildRelation(childCapa)) {
        result.addError(new RemoteResourceSyncFrameworkException(
          String.format("invalid child relation: Expected %s, found '%s'",
            Arrays.toString(capability.getChildRelationsXmlValues()),
            childCapa == null ? "<no relation>" : childCapa.xmlValue)));
      }

      // child relation to document of same capability only allowed if document is sitemapIndex
      if (capability != null && capability == childCapa) {
        if (!result.getContent().map(content -> content instanceof Sitemapindex).orElse(true)) {
          result.addError(new RemoteResourceSyncFrameworkException(
            String.format("invalid child relation: relation to same capability '%s' " +
              "and document is not '<sitemapindex>'", capability.xmlValue)));
        }
      }
    }
  }

  private ResourceSyncContext getRsContext() {
    return rsContext;
  }

  private LambdaExceptionUtil.Function_WithExceptions<HttpResponse, RsRoot, Exception> rsConverter = (response) -> {
    InputStream inStream = response.getEntity().getContent();
    return new RsBuilder(this.getRsContext()).setInputStream(inStream).build().orElse(null);
  };

  private LambdaExceptionUtil.Function_WithExceptions<HttpResponse, RsRoot, Exception> getSitemapConverter() {
    if (sitemapConverter == null) {
      sitemapConverter = rsConverter;
    }
    return sitemapConverter;
  }

  private LambdaExceptionUtil.Function_WithExceptions<HttpResponse, Description, Exception> descriptionReader =
    (response) -> {
      InputStream inStream = response.getEntity().getContent();
      String encoding = AbstractUriExplorer.getCharset(response);
      return new Description(IOUtils.toString(inStream, encoding));
    };

}
