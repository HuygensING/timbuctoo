package nl.knaw.huygens.timbuctoo.index;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IndexFacade implements SearchManager, IndexManager {

  private static final Logger LOG = LoggerFactory.getLogger(IndexFacade.class);
  private final VREManager vreManager;
  private final Repository storageManager;
  private final SortableFieldFinder sortableFieldFinder;
  private final FacetedSearchResultConverter facetedSearchResultConverter;

  @Inject
  public IndexFacade(Repository storageManager, SortableFieldFinder sortableFieldFinder, FacetedSearchResultConverter facetedSearchResultConverter, VREManager vreManager) {
    this.storageManager = storageManager;
    this.sortableFieldFinder = sortableFieldFinder;
    this.facetedSearchResultConverter = facetedSearchResultConverter;
    this.vreManager = vreManager;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexAdder = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.add(variations);

      }
    };
    changeIndex(type, id, indexAdder);

  }

  private <T extends DomainEntity> void changeIndex(Class<T> type, String id, IndexChanger indexChanger) throws IndexException {
    Class<? extends DomainEntity> baseType1 = toBaseDomainEntity(type);
    Class<? extends DomainEntity> baseType = baseType1;
    List<? extends DomainEntity> variations = null;

    variations = storageManager.getAllVariations(baseType, id);
    if (variations == null || variations.isEmpty()) {
      throw new IndexException("Could not retrieve variations for type " + type + "with id " + id);
    }

    for (VRE vre : vreManager.getAllVREs()) {
      Index index = vreManager.getIndexFor(vre, type);
      List<? extends DomainEntity> filteredVariations = vre.filter(variations);

      indexChanger.executeIndexAction(index, filteredVariations);
    }
  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexUpdater = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.update(variations);
      }
    };
    changeIndex(type, id, indexUpdater);
  }

  @Override
  public <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException {
    for (VRE vre : vreManager.getAllVREs()) {
      Index index = vreManager.getIndexFor(vre, type);

      index.deleteById(id);
    }

  }

  @Override
  public <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    for (VRE vre : vreManager.getAllVREs()) {
      Index index = vreManager.getIndexFor(vre, type);

      index.deleteById(ids);
    }

  }

  @Override
  public void deleteAllEntities() throws IndexException {
    for (Index index : vreManager.getAllIndexes()) {
      index.clear();
    }
  }

  @Override
  public IndexStatus getStatus() {
    IndexStatus indexStatus = creatIndexStatus();

    for (VRE vre : vreManager.getAllVREs()) {
      for (Class<? extends DomainEntity> type : vre.getBaseEntityTypes()) {
        Index index = vreManager.getIndexFor(vre, type);
        try {
          indexStatus.addCount(vre, type, index.getCount());
        } catch (IndexException e) {
          LOG.error("Failed to obtain status: {}", e.getMessage());
        }
      }
    }

    return indexStatus;
  }

  protected IndexStatus creatIndexStatus() {
    return new IndexStatus();
  }

  @Override
  public void commitAll() throws IndexException {
    for (Index index : vreManager.getAllIndexes()) {
      index.commit();
    }
  }

  @Override
  public void close() throws IndexException {
    for (Index index : vreManager.getAllIndexes()) {
      try {
        index.close();
      } catch (IndexException ex) {
        LOG.error("closing of index {} went wrong", index.getName(), ex);
      }
    }

  }

  @Override
  public Set<String> findSortableFields(Class<? extends DomainEntity> type) {
    return sortableFieldFinder.findFields(type);
  }

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search(VRE vre, Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) throws SearchException {
    Index index = vreManager.getIndexFor(vre, type);

    FacetedSearchResult facetedSearchResult = index.search(searchParameters);

    return facetedSearchResultConverter.convert(TypeNames.getInternalName(type), facetedSearchResult);
  }

  private static interface IndexChanger {
    void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException;
  }

  @Deprecated
  @Override
  public <T extends DomainEntity> QueryResponse search(VRE vre, Class<T> type, SolrQuery query) throws IndexException {
    // TODO Auto-generated method stub
    return null;
  }
}
