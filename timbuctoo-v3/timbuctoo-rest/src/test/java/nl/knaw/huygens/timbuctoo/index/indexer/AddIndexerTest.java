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

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AddIndexerTest  {

  private static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private static final String ID = "id";
  private AddIndexer instance;
  private IndexManager indexManager;

  @Before
  public void setup() {
    indexManager = mock(IndexManager.class);
    instance = new AddIndexer(indexManager);
  }

  @Test
  public void executeIndexActionClassIndexManagersAddEntity() throws Exception {
    // action
    instance.executeIndexAction(TYPE, ID);

    // verify
    verify(indexManager).addEntity(TYPE, ID);
  }

  @Test(expected = IndexException.class)
  public void executeIndexActionThrowsAnIndexExctionWhenTheIndexManagerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexManager).addEntity(TYPE, ID);

    // action
    instance.executeIndexAction(TYPE, ID);
  }
}
