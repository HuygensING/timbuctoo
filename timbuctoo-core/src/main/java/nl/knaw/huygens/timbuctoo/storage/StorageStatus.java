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

  private final List<KV<Long>> domainEntityStats;
  private final List<KV<Long>> systemEntityStats;

  public StorageStatus() {
    domainEntityStats = Lists.newArrayList();
    systemEntityStats = Lists.newArrayList();
  }

  public List<KV<Long>> getDomainEntityStats() {
    return domainEntityStats;
  }

  public void addDomainEntityStats(KV<Long> stats) {
    domainEntityStats.add(stats);
  }

  public List<KV<Long>> getSystemEntityStats() {
    return systemEntityStats;
  }

  public void addSystemEntityStats(KV<Long> stats) {
    systemEntityStats.add(stats);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("System entity counts\n");
    for (KV<Long> kv : systemEntityStats) {
      builder.append(String.format("- %-20s %6d\n", kv.getKey(), kv.getValue()));
    }
    builder.append("Domain entity counts\n");
    for (KV<Long> kv : domainEntityStats) {
      builder.append(String.format("- %-20s %6d\n", kv.getKey(), kv.getValue()));
    }
    return builder.toString();
  }

}
