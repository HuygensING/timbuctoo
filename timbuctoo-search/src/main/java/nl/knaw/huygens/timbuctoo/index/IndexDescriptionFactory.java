package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescriptionBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetFinder;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.IndexedFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

public class IndexDescriptionFactory {

  private final IndexDescriptionBuilder indexDescriptionBuilder;
  private final FacetFinder facetFinder;
  private final FullTextSearchFieldFinder fullTextSearchFieldFinder;
  private final IndexedFieldFinder indexedFieldFinder;
  private final SortableFieldFinder sortableFieldFinder;

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
