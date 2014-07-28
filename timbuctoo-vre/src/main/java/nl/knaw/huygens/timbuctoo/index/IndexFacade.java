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

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IndexFacade implements IndexManager {

  private static final Logger LOG = LoggerFactory.getLogger(IndexFacade.class);

  private final VREManager vreManager;
  private final Repository storageManager;

  @Inject
  public IndexFacade(Repository storageManager, VREManager vreManager) {
    this.storageManager = storageManager;
    this.vreManager = vreManager;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexAdder = new IndexChanger() {
      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.add(variations);
      }
    };
    changeIndex(type, id, indexAdder);
  }

  private <T extends DomainEntity> void changeIndex(Class<T> type, String id, IndexChanger indexChanger) throws IndexException {
    Class<? extends DomainEntity> baseType = toBaseDomainEntity(type);
    List<? extends DomainEntity> variations = storageManager.getAllVariations(baseType, id);
    if (!variations.isEmpty()) {
      for (VRE vre : vreManager.getAllVREs()) {
        Index index = vreManager.getIndexFor(vre, type);
        indexChanger.executeIndexAction(index, vre.filter(variations));
      }
    }
  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexUpdater = new IndexChanger() {
      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.update(variations);
      }
    };
    changeIndex(type, id, indexUpdater);
  }

  @Override
  public <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException {
    for (VRE vre : vreManager.getAllVREs()) {
      Index index = vreManager.getIndexFor(vre, type);
      index.deleteById(id);
    }
  }

  @Override
  public <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    for (VRE vre : vreManager.getAllVREs()) {
      Index index = vreManager.getIndexFor(vre, type);
      index.deleteById(ids);
    }
  }

  @Override
  public void deleteAllEntities() throws IndexException {
    for (Index index : vreManager.getAllIndexes()) {
      index.clear();
    }
  }

  @Override
  public IndexStatus getStatus() {
    IndexStatus indexStatus = createIndexStatus();

    for (VRE vre : vreManager.getAllVREs()) {
      for (Class<? extends DomainEntity> type : vre.getBaseEntityTypes()) {
        Index index = vreManager.getIndexFor(vre, type);
        try {
          indexStatus.addCount(vre, type, index.getCount());
        } catch (IndexException e) {
          LOG.error("Failed to obtain status: {}", e.getMessage());
        }
      }
    }

    return indexStatus;
  }

  protected IndexStatus createIndexStatus() {
    return new IndexStatus();
  }

  @Override
  public void commitAll() throws IndexException {
    for (Index index : vreManager.getAllIndexes()) {
      index.commit();
    }
  }

  @Override
  public void close() {
    for (Index index : vreManager.getAllIndexes()) {
      try {
        index.close();
      } catch (IndexException ex) {
        LOG.error("closing of index {} went wrong", index.getName(), ex);
      }
    }
  }

  private static interface IndexChanger {
    void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException;
  }

}
