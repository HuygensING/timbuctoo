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

import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractIndexRequestTest {
  public static final ActionType ACTION_TYPE = ActionType.ADD;
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private Indexer indexer;
  private IndexRequest instance;
  private IndexerFactory indexerFactory;

  @Before
  public void setup() {
    setupIndexerFactory();
    instance = createInstance();
  }

  private void setupIndexerFactory() {
    indexer = mock(Indexer.class);
    indexerFactory = mock(IndexerFactory.class);
    when(indexerFactory.create(ACTION_TYPE)).thenReturn(indexer);
  }

  protected IndexRequest getInstance() {
    return instance;
  }

  protected abstract IndexRequest createInstance();

  protected IndexerFactory getIndexerFactory() {

    return indexerFactory;
  }

  protected Indexer getIndexer() {
    return indexer;
  }

  @Test
  abstract void toActionCreatesAnActionThatCanBeUsedByTheProducer();
}
