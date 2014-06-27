package nl.knaw.huygens.timbuctoo.index;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.core.CoreDescriptor;
import org.junit.Test;

import test.timbuctoo.index.model.Type1;

public class SolrIndexFactoryTest {
  @Test
  public void testCreate() {
    // It should create a list of facet definitions.
    // It should create an AbstractSolrServer.
    // It should create a FacetedSearchLibrary

    // setup
    AbstractSolrServer solrServerMock = mock(AbstractSolrServer.class);
    IndexDescription facetDefinitions = mock(IndexDescription.class);
    FacetedSearchLibrary facetedSearchLibraryMock = mock(FacetedSearchLibrary.class);
    SolrInputDocumentCreator solrInputDocumentCreatorMock = mock(SolrInputDocumentCreator.class);

    IndexDescriptionFactory indexDescriptionFactoryMock = mock(IndexDescriptionFactory.class);
    AbstractSolrServerBuilder solrServerBuilderMock = mock(AbstractSolrServerBuilder.class);
    FacetedSearchLibraryFactory facetedSearchLibraryFactoryMock = mock(FacetedSearchLibraryFactory.class);

    String name = "test";
    Class<? extends DomainEntity> type = Type1.class;

    Index expectedSolrIndex = new SolrIndex(name, solrInputDocumentCreatorMock, solrServerMock, facetedSearchLibraryMock);

    when(indexDescriptionFactoryMock.create(type)).thenReturn(facetDefinitions);
    when(solrServerBuilderMock.setCoreName(name)).thenReturn(solrServerBuilderMock);
    when(solrServerBuilderMock.build(facetDefinitions)).thenReturn(solrServerMock);
    when(solrServerBuilderMock.addProperty(CoreDescriptor.CORE_DATADIR, "data/" + name.replace('.', '/'))).thenReturn(solrServerBuilderMock);
    when(facetedSearchLibraryFactoryMock.create(solrServerMock)).thenReturn(facetedSearchLibraryMock);

    SolrIndexFactory instance = new SolrIndexFactory(solrInputDocumentCreatorMock, solrServerBuilderMock, indexDescriptionFactoryMock, facetedSearchLibraryFactoryMock);

    // action
    SolrIndex actualSolrIndex = instance.createIndexFor(type, name);

    // verify
    assertThat(actualSolrIndex, is(equalTo(expectedSolrIndex)));
  }
}
