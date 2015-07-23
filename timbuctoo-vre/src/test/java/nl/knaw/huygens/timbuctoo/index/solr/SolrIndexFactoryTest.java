package nl.knaw.huygens.timbuctoo.index.solr;

/*
 * #%L
 * Timbuctoo VRE
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

import static nl.knaw.huygens.timbuctoo.index.solr.SolrIndexFactory.SOLR_DATA_DIR_CONFIG_PROP;
import static nl.knaw.huygens.timbuctoo.vre.VREMockBuilder.newVRE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.index.RawSearchFieldFactory;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchLibraryFactory;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.solr.core.CoreDescriptor;
import org.junit.Before;
import org.junit.Test;

import test.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import test.timbuctoo.index.model.SubModel;
import test.timbuctoo.index.model.Type1;

public class SolrIndexFactoryTest {

  private static final String RAW_SEARCH_FIELD = "rawSearchField";

  private static final String DATA_DIR = "data/";

  private AbstractSolrServer solrServerMock;
  private IndexDescription indexDescription;
  private FacetedSearchLibrary facetedSearchLibraryMock;
  private SolrInputDocumentCreator solrInputDocumentCreatorMock;
  private Configuration configurationMock;
  private IndexDescriptionFactory indexDescriptionFactoryMock;
  private AbstractSolrServerBuilder solrServerBuilderMock;
  private FacetedSearchLibraryFactory facetedSearchLibraryFactoryMock;

  private SolrIndexFactory instance;

  private RawSearchFieldFactory rawSearchFieldFactoryMock;

  @Before
  public void setup() {
    solrServerMock = mock(AbstractSolrServer.class);
    indexDescription = mock(IndexDescription.class);
    facetedSearchLibraryMock = mock(FacetedSearchLibrary.class);
    solrInputDocumentCreatorMock = mock(SolrInputDocumentCreator.class);
    configurationMock = mock(Configuration.class);
    indexDescriptionFactoryMock = mock(IndexDescriptionFactory.class);
    solrServerBuilderMock = mock(AbstractSolrServerBuilder.class);
    facetedSearchLibraryFactoryMock = mock(FacetedSearchLibraryFactory.class);
    rawSearchFieldFactoryMock = mock(RawSearchFieldFactory.class);

    instance = new SolrIndexFactory(solrInputDocumentCreatorMock, solrServerBuilderMock, indexDescriptionFactoryMock, facetedSearchLibraryFactoryMock, configurationMock, rawSearchFieldFactoryMock);
  }

  @Test
  public void testCreateIndex() {
    // setup
    String scopeId = "scopeid";
    VRE vre = newVRE().withVreId(scopeId).create();

    Class<? extends DomainEntity> type = Type1.class;
    String indexName = instance.getIndexNameFor(vre, type);

    Index expectedSolrIndex = new SolrIndex(indexName, RAW_SEARCH_FIELD, indexDescription, solrInputDocumentCreatorMock, solrServerMock, facetedSearchLibraryMock);

    when(rawSearchFieldFactoryMock.getRawSearchField(type)).thenReturn(RAW_SEARCH_FIELD);
    when(indexDescriptionFactoryMock.create(type)).thenReturn(indexDescription);
    when(solrServerBuilderMock.setCoreName(indexName)).thenReturn(solrServerBuilderMock);
    when(solrServerBuilderMock.build(indexDescription)).thenReturn(solrServerMock);
    when(configurationMock.getSetting(SOLR_DATA_DIR_CONFIG_PROP)).thenReturn(DATA_DIR);
    when(solrServerBuilderMock.addProperty(CoreDescriptor.CORE_DATADIR, DATA_DIR + "/" + indexName.replace('.', '/'))).thenReturn(solrServerBuilderMock);
    when(facetedSearchLibraryFactoryMock.create(solrServerMock)).thenReturn(facetedSearchLibraryMock);

    // action
    SolrIndex actualSolrIndex = instance.createIndexFor(vre, type);

    // verify
    assertThat(actualSolrIndex, equalTo(expectedSolrIndex));
  }

  @Test
  public void testGetIndexNameForBaseType() {
    VRE vre = newVRE().withVreId("vre1").create();
    Class<? extends DomainEntity> type = ExplicitlyAnnotatedModel.class;

    assertThat(instance.getIndexNameFor(vre, type), equalTo("vre1.explicitlyannotatedmodel"));
  }

  @Test
  public void testGetIndexNameForSubType() {
    VRE vre = newVRE().withVreId("vre2").create();
    Class<? extends DomainEntity> type = SubModel.class;

    assertThat(instance.getIndexNameFor(vre, type), equalTo("vre2.explicitlyannotatedmodel"));
  }

}
