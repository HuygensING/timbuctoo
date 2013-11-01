package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.KV;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IndexStatus {

  private final Map<String, List<KV<Long>>> counts;

  public IndexStatus() {
    counts = Maps.newTreeMap();
  }

  public Map<String, List<KV<Long>>> getCounts() {
    return counts;
  }

  public void addCount(Scope scope, Class<? extends DomainEntity> type, long count) {
    List<KV<Long>> list = counts.get(scope.getId());
    if (list == null) {
      list = Lists.newArrayList();
      counts.put(scope.getId(), list);
    }
    list.add(new KV<Long>(type.getSimpleName(), count));
  }

}
