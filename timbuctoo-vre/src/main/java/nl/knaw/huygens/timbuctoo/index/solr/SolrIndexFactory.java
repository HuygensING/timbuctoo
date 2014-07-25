package nl.knaw.huygens.timbuctoo.index.solr;

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

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchLibraryFactory;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.solr.core.CoreDescriptor;

import com.google.inject.Inject;

public class SolrIndexFactory implements IndexFactory {
  public static final String SOLR_DATA_DIR_CONFIG_PROP = "solr.data_dir";

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServerBuilder solrServerBuilder;
  private final IndexDescriptionFactory indexDescriptionFactory;
  private final FacetedSearchLibraryFactory facetedSearchLibraryFactory;
  private final IndexNameCreator indexNameCreator;
  private final Configuration configuration;

  //FIXME some abstractions are missing, there are too many dependencies.

  @Inject
  public SolrIndexFactory(SolrInputDocumentCreator solrInputDocumentCreator, AbstractSolrServerBuilder solrServerBuilder, IndexDescriptionFactory indexDescriptionFactory,
      FacetedSearchLibraryFactory facetedSearchLibraryFactory, IndexNameCreator indexNameCreator, Configuration configuration) {
    this.solrDocumentCreator = solrInputDocumentCreator;
    this.solrServerBuilder = solrServerBuilder;
    this.indexDescriptionFactory = indexDescriptionFactory;
    this.facetedSearchLibraryFactory = facetedSearchLibraryFactory;
    this.indexNameCreator = indexNameCreator;
    this.configuration = configuration;
  }

  private String getSolrDataDir(String name) {

    return String.format("%s/%s", configuration.getSetting(SOLR_DATA_DIR_CONFIG_PROP), name.replace('.', '/'));
  }

  @Override
  public SolrIndex createIndexFor(VRE vre, Class<? extends DomainEntity> type) {
    String name = indexNameCreator.getIndexNameFor(vre, type);
    IndexDescription indexDescription = this.indexDescriptionFactory.create(type);
    AbstractSolrServer abstractSolrServer = this.solrServerBuilder.setCoreName(name) //
        .addProperty(CoreDescriptor.CORE_DATADIR, getSolrDataDir(name)) //
        .build(indexDescription);
    FacetedSearchLibrary facetedSearchLibrary = this.facetedSearchLibraryFactory.create(abstractSolrServer);

    return new SolrIndex(name, solrDocumentCreator, abstractSolrServer, facetedSearchLibrary);
  }
}
