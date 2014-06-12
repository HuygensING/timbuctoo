package nl.knaw.huygens.timbuctoo.index;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.collect.Maps;

public class IndexMapCreator {

  private final IndexNameCreator indexNameCreator;
  private final IndexFactory indexFactory;

  public IndexMapCreator(IndexNameCreator indexNameCreator, IndexFactory indexFactory) {
    this.indexNameCreator = indexNameCreator;
    this.indexFactory = indexFactory;

  }

  public Map<String, Index> createIndexesFor(VRE vre) {
    Map<String, Index> indexMap = createIndexMap();

    for (Class<? extends DomainEntity> type : vre.getBaseEntityTypes()) {
      String typeName = indexNameCreator.getIndexNameFor(vre, type);
      indexMap.put(typeName, indexFactory.createIndexFor(typeName));
    }
    return indexMap;
  }

  protected Map<String, Index> createIndexMap() {
    return Maps.newHashMap();
  }

}
