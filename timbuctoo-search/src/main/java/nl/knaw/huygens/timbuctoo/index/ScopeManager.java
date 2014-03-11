package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.Scope;

public class ScopeManager {

  private final List<Scope> scopes;
  private Map<String, Index> indexes;
  private final IndexNameCreator indexNameCreator;

  protected ScopeManager(List<Scope> scopes, IndexNameCreator indexNameCreator) {
    this.scopes = scopes;
    this.indexNameCreator = indexNameCreator;
    indexes = createIndexes();
  }

  public List<Scope> getAllScopes() {
    return scopes;
  }

  public Index getIndexFor(Scope scope, Class<? extends DomainEntity> type) {
    String indexName = indexNameCreator.getIndexNameFor(scope, type);

    Index index = indexes.get(indexName);
    if (index == null) {
      index = new NoOpIndex();
    }

    return index;
  }

  protected Map<String, Index> createIndexes() {
    return null;
  }

  protected static class NoOpIndex implements Index {

    @Override
    public void add(Class<? extends DomainEntity> type, String id) {

    }

  }

}
