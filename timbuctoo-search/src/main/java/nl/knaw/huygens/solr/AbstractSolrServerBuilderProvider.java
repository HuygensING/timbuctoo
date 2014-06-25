package nl.knaw.huygens.solr;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.File;

import nl.knaw.huygens.solr.AbstractSolrServerBuilder.SolrServerType;
import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.apache.solr.core.CoreDescriptor;

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
      builder.setSolrDir(solrDir).addProperty(CoreDescriptor.CORE_LOADONSTARTUP, true).setConfigFile(getSolrConfigFile(solrDir));
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

  protected File getSolrConfigFile(String solrDir) {
    return new File(new File(solrDir, "conf"), "solr.xml");
  }

}
