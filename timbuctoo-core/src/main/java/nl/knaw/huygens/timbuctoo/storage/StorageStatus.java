package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("System entity counts\n");
    for (KV<Long> kv : systemEntityCounts) {
      builder.append(String.format("- %-20s %6d\n", kv.getKey(), kv.getValue()));
    }
    builder.append("Domain entity counts\n");
    for (KV<Long> kv : domainEntityCounts) {
      builder.append(String.format("- %-20s %6d\n", kv.getKey(), kv.getValue()));
    }
    return builder.toString();
  }

}
