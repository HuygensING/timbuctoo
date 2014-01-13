package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (String key : counts.keySet()) {
      builder.append("Indexed scope '").append(key).append("'\n");
      for (KV<Long> kv : counts.get(key)) {
        builder.append(String.format("- %-20s %6d\n", kv.getKey(), kv.getValue()));
      }
    }
    return builder.toString();
  }

}
