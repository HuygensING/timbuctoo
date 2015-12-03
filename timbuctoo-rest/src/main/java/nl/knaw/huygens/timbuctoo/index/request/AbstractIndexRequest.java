package nl.knaw.huygens.timbuctoo.index.request;

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

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

abstract class AbstractIndexRequest implements IndexRequest {

  private final IndexerFactory indexerFactory;
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;

  protected AbstractIndexRequest(IndexerFactory indexerFactory, ActionType actionType, Class<? extends DomainEntity> type) {
    this.indexerFactory = indexerFactory;
    this.actionType = actionType;
    this.type = type;
  }

  protected Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public abstract void execute() throws IndexException;

  protected IndexerFactory getIndexerFactory(){
    return this.indexerFactory;
  }

  protected ActionType getActionType() {
    return actionType;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
