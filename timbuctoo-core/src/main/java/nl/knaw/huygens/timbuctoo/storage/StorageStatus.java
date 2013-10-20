package nl.knaw.huygens.timbuctoo.storage;

import java.util.List;

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

  // -------------------------------------------------------------------

  public static class KV<T> {
    private final String key;
    private final T value;

    public KV(String key, T value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public T getValue() {
      return value;
    }
  }

}
