package nl.knaw.huygens.timbuctoo.tools.config;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.persistence.PersistenceManagerCreationException;
import nl.knaw.huygens.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexFacade;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.NoOpIndex;
import nl.knaw.huygens.timbuctoo.index.solr.SolrIndexFactory;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.OaiPmhRestClient;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import nl.knaw.huygens.timbuctoo.vre.VREs;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * A class to make it possible to use Guice @see http://code.google.com/p/google-guice.
 */
public class ToolsInjectionModule extends BasicInjectionModule {
  private boolean useSolr;
  private static Configuration config;

  public static Injector createInjector() throws ConfigurationException {
    config = getConfiguration();
    return Guice.createInjector(new ToolsInjectionModule(config, true));
  }

  public static Injector createInjectorWithoutSolr() throws ConfigurationException {
    config = getConfiguration();
    return Guice.createInjector(new ToolsInjectionModule(config, false));
  }

  private static Configuration getConfiguration() throws ConfigurationException {
    Configuration config = new Configuration("config.xml");
    return config;
  }

  public ToolsInjectionModule(Configuration config, boolean useSolr) {
    super(config);
    this.useSolr = useSolr;
  }

  @Override
  protected void configure() {
    super.configure();
    bind(IndexManager.class).to(IndexFacade.class);
    if (useSolr) {
      bind(IndexFactory.class).to(SolrIndexFactory.class);

    } else {
      bind(IndexFactory.class).to(NoOpIndexFactory.class);
    }
    bind(VRECollection.class).to(VREs.class);
    bind(AbstractSolrServerBuilder.class).toProvider(AbstractSolrServerBuilderProvider.class);
  }

  @Singleton
  @Provides
  public OaiPmhRestClient providesOaiPmhRestClient() {
    return new OaiPmhRestClient(config.getSetting("oai-url"));
  }

  @Provides
  @Singleton
  PersistenceManager providePersistenceManager() throws PersistenceManagerCreationException {
    PersistenceManager persistenceManager = PersistenceManagerFactory.newPersistenceManager(config.getBooleanSetting("handle.enabled", true), config.getSetting("handle.cipher"),
        config.getSetting("handle.naming_authority"), config.getSetting("handle.prefix"), config.getSetting("handle.private_key_file"));
    return persistenceManager;
  }

  static class NoOpIndexFactory implements IndexFactory {
    private static final NoOpIndex NO_OP_INDEX = new NoOpIndex();
    private static final Logger LOG = LoggerFactory.getLogger(NoOpIndexFactory.class);

    @Override
    public Index createIndexFor(VRE vre, Class<? extends DomainEntity> type) {
      LOG.info("Creating a no op index for vre \"{}\" and type \"{}\"", vre.getVreId(), type.getSimpleName());
      return NO_OP_INDEX;
    }

  }
}
