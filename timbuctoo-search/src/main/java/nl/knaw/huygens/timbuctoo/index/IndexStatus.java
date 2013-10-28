package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.util.KV;

import com.google.common.collect.Lists;

public class IndexStatus {

  private final List<KV<Long>> domainEntityCounts;

  public IndexStatus() {
    domainEntityCounts = Lists.newArrayList();
  }

  public List<KV<Long>> getDomainEntityCounts() {
    return domainEntityCounts;
  }

  public void addDomainEntityCount(KV<Long> count) {
    domainEntityCounts.add(count);
  }

}
