package nl.knaw.huygens.timbuctoo;

/*
 * #%L
 * Timbuctoo tools
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

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.storage.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * "Extended" repository, work in progress.
 * The index manager will be integrated with VRE's.
 */
public class XRepository {

  private static final Logger LOG = LoggerFactory.getLogger(XRepository.class);

  private final Repository repository;
  private final IndexManager indexManager;

  @Inject
  public XRepository(Repository repository, IndexManager indexManager) {
    this.repository = repository;
    this.indexManager = indexManager;
  }

  public Repository getStorageManager() {
    return repository;
  }

  public IndexManager getIndexManager() {
    return indexManager;
  }

  public void close() {
    repository.close();
    try {
      indexManager.close();
    } catch (IndexException e) {
      LOG.error("Error while closing index: {}", e.getMessage());
    }
  }

}
