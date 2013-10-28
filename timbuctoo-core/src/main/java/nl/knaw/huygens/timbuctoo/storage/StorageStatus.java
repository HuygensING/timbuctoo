package nl.knaw.huygens.timbuctoo.storage;

import java.util.List;

import nl.knaw.huygens.timbuctoo.util.KV;

import com.google.common.collect.Lists;

public class StorageStatus {

  private final List<KV<Long>> domainEntityCounts;
  private final List<KV<Long>> systemEntityCounts;

  public StorageStatus() {
    domainEntityCounts = Lists.newArrayList();
    systemEntityCounts = Lists.newArrayList();
  }

  public List<KV<Long>> getDomainEntityCounts() {
    return domainEntityCounts;
  }

  public void addDomainEntityCount(KV<Long> count) {
    domainEntityCounts.add(count);
  }

  public List<KV<Long>> getSystemEntityCounts() {
    return systemEntityCounts;
  }

  public void addSystemEntityCount(KV<Long> count) {
    systemEntityCounts.add(count);
  }

}
