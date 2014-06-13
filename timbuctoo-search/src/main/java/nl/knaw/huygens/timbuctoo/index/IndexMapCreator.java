package nl.knaw.huygens.timbuctoo.index;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IndexMapCreator {

  private final IndexNameCreator indexNameCreator;
  private final IndexFactory indexFactory;

  @Inject
  public IndexMapCreator(IndexNameCreator indexNameCreator, IndexFactory indexFactory) {
    this.indexNameCreator = indexNameCreator;
    this.indexFactory = indexFactory;
  }

  public Map<String, Index> createIndexesFor(VRE vre) {
    Map<String, Index> indexMap = createIndexMap();

    for (Class<? extends DomainEntity> type : vre.getBaseEntityTypes()) {
      String indexName = indexNameCreator.getIndexNameFor(vre, type);
      indexMap.put(indexName, indexFactory.createIndexFor(type, indexName));
    }
    return indexMap;
  }

  protected Map<String, Index> createIndexMap() {
    return Maps.newHashMap();
  }

}
