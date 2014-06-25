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

import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescriptionBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetFinder;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.IndexedFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.inject.Inject;

public class IndexDescriptionFactory {

  private final IndexDescriptionBuilder indexDescriptionBuilder;
  private final FacetFinder facetFinder;
  private final FullTextSearchFieldFinder fullTextSearchFieldFinder;
  private final IndexedFieldFinder indexedFieldFinder;
  private final SortableFieldFinder sortableFieldFinder;

  @Inject
  public IndexDescriptionFactory( // 
      IndexDescriptionBuilder indexDescriptionBuilder, //
      FacetFinder facetFinder, //
      FullTextSearchFieldFinder fullTextSearchFieldFinder, //
      IndexedFieldFinder indexedFieldFinder, //
      SortableFieldFinder sortableFieldFinder) {

    this.indexDescriptionBuilder = indexDescriptionBuilder;
    this.facetFinder = facetFinder;
    this.fullTextSearchFieldFinder = fullTextSearchFieldFinder;
    this.indexedFieldFinder = indexedFieldFinder;
    this.sortableFieldFinder = sortableFieldFinder;
  }

  public IndexDescription create(Class<? extends DomainEntity> type) {
    indexDescriptionBuilder.setFacetDefinitions(facetFinder.findFacetDefinitions(type)) //
        .setFullTextSearchFields(fullTextSearchFieldFinder.findFields(type)) //
        .setIndexedFields(indexedFieldFinder.findFields(type)) //
        .setSortFields(sortableFieldFinder.findFields(type));

    return indexDescriptionBuilder.build();
  }

}
