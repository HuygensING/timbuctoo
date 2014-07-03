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

import java.util.List;

import nl.knaw.huygens.timbuctoo.index.ModelIterator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.common.SolrInputDocument;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A class an input document for the types in the scope.
 */
@Singleton
public class SolrInputDocumentCreator {

  private final ModelIterator modelIterator;

  @Inject
  public SolrInputDocumentCreator(ModelIterator modelIterator) {
    this.modelIterator = modelIterator;
  }

  public SolrInputDocumentCreator() {
    this(new ModelIterator());
  }

  public <T extends DomainEntity> SolrInputDocument create(List<T> entities) {
    SolrInputDocument document = null;
    for (T entity : entities) {
      SolrInputDocGenerator docGenerator = createSolerInputDocGenerator(entity, document);
      modelIterator.processClass(docGenerator, entity.getClass());
      document = docGenerator.getResult();
    }
    return document;

  }

  protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument document) {
    SolrInputDocGenerator indexer = null;

    if (document == null) {
      indexer = new SolrInputDocGenerator(entity);
    } else {
      indexer = new SolrInputDocGenerator(entity, document);
    }
    return indexer;
  }

}
