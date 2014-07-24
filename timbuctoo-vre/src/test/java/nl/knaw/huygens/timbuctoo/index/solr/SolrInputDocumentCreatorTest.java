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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.timbuctoo.index.ModelIterator;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.index.solr.SolrInputDocGenerator;
import nl.knaw.huygens.timbuctoo.index.solr.SolrInputDocumentCreator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SolrInputDocumentCreatorTest {
  @Test
  public void testCreateForOneEntity() {
    // mock
    final SolrInputDocGenerator solrInputDocGenerator = mock(SolrInputDocGenerator.class);
    ModelIterator modelIteratorMock = mock(ModelIterator.class);

    SolrInputDocumentCreator instance = new SolrInputDocumentCreator(modelIteratorMock) {
      @Override
      protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument doc) {
        return solrInputDocGenerator;
      }
    };

    List<DomainEntity> variationsToAdd = Lists.newArrayList();
    variationsToAdd.add(new ExplicitlyAnnotatedModel());

    // action
    instance.create(variationsToAdd);

    // verify
    verify(modelIteratorMock).processClass(solrInputDocGenerator, ExplicitlyAnnotatedModel.class);
  }

  @Test
  public void testCreateForMultipleEntities() {
    // mock
    final SolrInputDocGenerator solrInputDocGenerator = mock(SolrInputDocGenerator.class);
    ModelIterator modelIteratorMock = mock(ModelIterator.class);

    SolrInputDocumentCreator instance = new SolrInputDocumentCreator(modelIteratorMock) {
      @Override
      protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument doc) {
        return solrInputDocGenerator;
      }
    };

    List<DomainEntity> variationsToAdd = Lists.newArrayList();
    variationsToAdd.add(new ExplicitlyAnnotatedModel());
    variationsToAdd.add(new SubModel());

    // action
    instance.create(variationsToAdd);

    // verify
    verify(modelIteratorMock).processClass(solrInputDocGenerator, ExplicitlyAnnotatedModel.class);
    verify(modelIteratorMock).processClass(solrInputDocGenerator, SubModel.class);
  }

}
