package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> class, 
 * for missing indexes. 
 */
public class NoOpIndex implements Index {
  private static Logger LOG = LoggerFactory.getLogger(NoOpIndex.class);

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