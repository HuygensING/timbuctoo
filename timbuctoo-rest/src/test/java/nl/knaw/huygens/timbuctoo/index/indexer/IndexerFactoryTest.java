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

import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;

public class IndexerFactoryTest {

  private IndexerFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexerFactory(mock(IndexManager.class));
  }

  @Test
  public void createCreatesAnAddIndexerIfTheActionTypeIsADD() {
    // action
    Indexer indexer = instance.create(ActionType.ADD);

    // verify
    assertThat(indexer, is(instanceOf(AddIndexer.class)));
  }

  @Test
  public void createCreatesAnUpdateIndexerIfTheActionTypeIsMOD() {
    // action
    Indexer indexer = instance.create(ActionType.MOD);

    // verify
    assertThat(indexer, is(instanceOf(UpdateIndexer.class)));
  }

  @Test
  public void createCreatesADeleteIndexerIfTheActionTypeIsDEL(){
    // action
    Indexer indexer = instance.create(ActionType.DEL);

    // verify
    assertThat(indexer, is(Matchers.instanceOf(DeleteIndexer.class)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createsThrowsAnIllegalArgumentExceptionIfTheActionTypeIsEnd(){
    // action
    instance.create(ActionType.END);
  }
}
