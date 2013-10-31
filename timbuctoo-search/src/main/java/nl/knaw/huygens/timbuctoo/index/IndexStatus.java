package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.KV;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import com.google.common.collect.Lists;

public class IndexStatus {

  private final List<KV<Long>> domainEntityCounts;

  public IndexStatus() {
    domainEntityCounts = Lists.newArrayList();
  }

  public List<KV<Long>> getDomainEntityCounts() {
    return domainEntityCounts;
  }

  public void addDomainEntityCount(Scope scope, Class<? extends DomainEntity> type, long count) {
    if ("full".equals(scope.getId())) {
      domainEntityCounts.add(new KV<Long>(type.getSimpleName(), count));
    }
  }

}
