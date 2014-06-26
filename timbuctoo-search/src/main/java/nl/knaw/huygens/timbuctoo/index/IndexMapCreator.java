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

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IndexMapCreator {

  private final IndexNameCreator indexNameCreator;
  private final IndexFactory indexFactory;

  @Inject
  public IndexMapCreator(IndexNameCreator indexNameCreator, IndexFactory indexFactory) {
    this.indexNameCreator = indexNameCreator;
    this.indexFactory = indexFactory;
  }

  public Map<String, Index> createIndexesFor(VRE vre) {
    Map<String, Index> indexMap = createIndexMap();

    for (Class<? extends DomainEntity> type : vre.getBaseEntityTypes()) {
      String indexName = indexNameCreator.getIndexNameFor(vre, type);
      //FIXME: use project specific type for creating the index.
      indexMap.put(indexName, indexFactory.createIndexFor(type, indexName));
    }
    return indexMap;
  }

  protected Map<String, Index> createIndexMap() {
    return Maps.newHashMap();
  }

}
