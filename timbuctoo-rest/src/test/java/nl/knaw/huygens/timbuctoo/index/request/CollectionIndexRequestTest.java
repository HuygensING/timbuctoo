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

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionIndexRequestTest extends AbstractIndexRequestTest {

  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  private RequestItemStatus requestItemStatus;

  @Override
  protected IndexRequest createInstance() {
    createRequestItemStatus();
    return new CollectionIndexRequest(getIndexerFactory(), ACTION_TYPE, TYPE, createRepository());
  }

  private void createRequestItemStatus() {
    requestItemStatus = mock(RequestItemStatus.class);
    when(requestItemStatus.getToDo()).thenReturn(Lists.newArrayList(ID_1, ID_2));
  }

  private Repository createRepository() {
    Repository repository = mock(Repository.class);
    StorageIteratorStub<ProjectADomainEntity> interator = StorageIteratorStub.newInstance(createEntityWithId(ID_1), createEntityWithId(ID_2));
    when(repository.getDomainEntities(TYPE)).thenReturn(interator);

    return repository;
  }

  private ProjectADomainEntity createEntityWithId(String id) {
    return new ProjectADomainEntity(id);
  }

  @Test
  public void executeIndexesEveryEntityFoundByTheRepository() throws Exception {
    // action
    getInstance().execute();

    // verify
    verify(getIndexer()).executeIndexAction(TYPE, ID_1);
    verify(getIndexer()).executeIndexAction(TYPE, ID_2);
  }

  @Test(expected = IndexException.class)
  public void executeThrowsAnIndexExceptionWhenTheIndexerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(getIndexer()).executeIndexAction(TYPE, ID_1);

    // action
    getInstance().execute();
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = getInstance().toAction();

    // verify
    assertThat(action, likeAction() //
      .withForMultiEntitiesFlag(true) //
      .withType(TYPE) //
      .withActionType(ACTION_TYPE));
  }

}
