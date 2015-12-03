package nl.knaw.huygens.timbuctoo.index.indexer;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.messages.ActionType;

public class IndexerFactory {
  private final IndexManager indexManager;

  @Inject
  public IndexerFactory(IndexManager indexManager){
    this.indexManager = indexManager;
  }

  public Indexer create(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddIndexer(indexManager);
      case MOD:
        return new UpdateIndexer(indexManager);
      case DEL:
        return new DeleteIndexer(indexManager);
      default:
        throw new IllegalArgumentException(String.format("[%s] is not supported by the IndexFactory.", actionType));
    }
  }

}
