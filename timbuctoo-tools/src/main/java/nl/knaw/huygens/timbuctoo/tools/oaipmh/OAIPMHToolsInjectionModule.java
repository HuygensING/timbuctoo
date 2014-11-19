package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.oaipmh.OaiPmhRestClient;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexFacade;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import nl.knaw.huygens.timbuctoo.vre.VREs;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class OAIPMHToolsInjectionModule extends BasicInjectionModule {
  static final String OAI_URL = "http://127.0.0.1:9998"; //TODO make configurable

  public OAIPMHToolsInjectionModule(Configuration config) {
    super(config);
  }

  @Provides
  public OaiPmhRestClient providesOaiPmhRestClient() {
    return new OaiPmhRestClient(OAI_URL);
  }

  public static Injector createOAIInjector() throws ConfigurationException {
    Configuration config = new Configuration("config.xml");
    return Guice.createInjector(new OAIPMHToolsInjectionModule(config));
  }

  @Override
  protected void configure() {
    super.configure();
    bind(IndexManager.class).to(IndexFacade.class);
    bind(IndexFactory.class).to(NoOpIndexFactory.class);
    bind(VRECollection.class).to(VREs.class);
    bind(AbstractSolrServerBuilder.class).toProvider(AbstractSolrServerBuilderProvider.class);
  }

  /**
   * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> class, 
   * for missing indexes. 
   */
  static class NoOpIndex implements Index {

    @Override
    public void add(List<? extends DomainEntity> variations) {}

    @Override
    public void update(List<? extends DomainEntity> variations) throws IndexException {}

    @Override
    public void deleteById(String id) {}

    @Override
    public void deleteById(List<String> ids) {}

    @Override
    public void clear() {}

    @Override
    public long getCount() {
      return 0;
    }

    @Override
    public void commit() {}

    @Override
    public void close() {}

    @Override
    public String getName() {
      return null;
    }

    @Override
    public <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParamaters) {
      return new FacetedSearchResult();
    }
  }

  static class NoOpIndexFactory implements IndexFactory {
    private static final Logger LOG = LoggerFactory.getLogger(NoOpIndexFactory.class);

    @Override
    public Index createIndexFor(VRE vre, Class<? extends DomainEntity> type) {
      LOG.info("Creating a no op index for vre \"{}\" and type \"{}\"", vre.getVreId(), type.getSimpleName());
      return new NoOpIndex();
    }

  }
}
