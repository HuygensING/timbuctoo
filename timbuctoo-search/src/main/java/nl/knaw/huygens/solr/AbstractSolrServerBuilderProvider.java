package nl.knaw.huygens.solr;

import nl.knaw.huygens.solr.AbstractSolrServerBuilder.SolrServerType;
import nl.knaw.huygens.timbuctoo.config.Configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AbstractSolrServerBuilderProvider implements Provider<AbstractSolrServerBuilder> {

  private final Configuration config;
  public static final String SOLR_URL = "solr.url";
  protected static final String SERVER_TYPE = "solr.server_type";
  protected static final String COMMIT_TIME = "solr.commit_within_seconds";

  @Inject
  public AbstractSolrServerBuilderProvider(Configuration config) {
    this.config = config;
  }

  @Override
  public AbstractSolrServerBuilder get() {

    SolrServerType serverType = getServerType();
    AbstractSolrServerBuilder builder = createAbstractSolrServer(serverType, config.getIntSetting(COMMIT_TIME));

    switch (serverType) {
      case LOCAL:
        String solrDir = config.getSolrHomeDir();
        builder.setSolrDir(solrDir);
        break;

      case REMOTE:
        builder.setSolrUrl(config.getSetting(SOLR_URL));
        break;

      default:
        throw new RuntimeException("Unknown solr server type: " + serverType);
    }

    return builder;

  }

  private SolrServerType getServerType() {
    return SolrServerType.valueOf(config.getSetting(SERVER_TYPE));
  }

  protected AbstractSolrServerBuilder createAbstractSolrServer(SolrServerType serverType, int commitTimeInSeconds) {

    return new AbstractSolrServerBuilder(serverType, commitTimeInSeconds);
  }

}
