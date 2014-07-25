package nl.knaw.huygens.timbuctoo.index;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class IndexCollection {

  private static final NoOpIndex NO_OP_INDEX = new NoOpIndex();

  private static final Logger LOG = LoggerFactory.getLogger(IndexCollection.class);

  private final Map<Class<? extends DomainEntity>, Index> indexMap;

  public IndexCollection() {
    indexMap = Maps.newHashMap();
  }

  /**
   * Returns the index if the index for the type can be found, 
   * else it returns an index that does nothing and returns an empty search result.
   * @param type the type to find the index for
   * @return the index
   */
  public Index getIndexByType(Class<? extends DomainEntity> type) {

    return indexMap.containsKey(toBaseDomainEntity(type)) ? indexMap.get(type) : NO_OP_INDEX;
  }

  /**
   * Add an index for a certain type.
   * @param type the type that belongs to the index.
   * @param index the index to add.
   */
  public void addIndex(Class<? extends DomainEntity> type, Index index) {
    indexMap.put(toBaseDomainEntity(type), index);
  }

  /**
   * A convenience method to create a new instance.
   * @param indexFactory creates then indexes.
   * @param vre the VRE to create to indexes for
   * @return a new instance of IndexCollection
   */
  public static IndexCollection create(IndexFactory indexFactory, VRE vre) {
    IndexCollection indexCollection = new IndexCollection();

    for (Class<? extends DomainEntity> type : vre.getEntityTypes()) {
      indexCollection.addIndex(type, indexFactory.createIndexFor(vre, type));
    }

    return indexCollection;
  }

  /**
   * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> class, 
   * for missing indexes. 
   */
  static class NoOpIndex implements Index {

    @Override
    public void add(List<? extends DomainEntity> variations) {}

    @Override
    public void update(List<? extends DomainEntity> variations) throws IndexException {}

    @Override
    public void deleteById(String id) {}

    @Override
    public void deleteById(List<String> ids) {}

    @Override
    public void clear() {}

    @Override
    public long getCount() {
      return 0;
    }

    @Override
    public void commit() {}

    @Override
    public void close() {}

    @Override
    public String getName() {
      return null;
    }

    @Override
    public <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParamaters) {
      LOG.warn("Searching on a non existing index");
      return new FacetedSearchResult();
    }
  }

  public Collection<Index> getAll() {
    // TODO Auto-generated method stub
    return null;
  }

}
