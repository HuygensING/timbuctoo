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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectARelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class IndexRequestFactoryTest {

  private static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private static final String ID = "id";
  private static final ActionType ACTION_TYPE = ActionType.MOD;
  private static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private IndexRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new IndexRequestFactory(mock(IndexerFactory.class), mock(Repository.class), mock(TypeRegistry.class));
  }

  @Test
  public void forCollectionOfCreatesACollectionIndexRequest() {
    // action
    IndexRequest indexRequest = instance.forCollectionOf(ActionType.MOD, TYPE);

    // verify
    assertThat(indexRequest, is(instanceOf(CollectionIndexRequest.class)));
  }

  @Test
  public void forEntityCreatesAnEntityIndexRequest() {
    // action
    IndexRequest indexRequest = instance.forEntity(ACTION_TYPE, TYPE, ID);

    // verify
    assertThat(indexRequest, is(instanceOf(EntityIndexRequest.class)));
  }

  @Test
  public void forEntityCreatesForARelationARelationIndexRequest() {
    // action
    IndexRequest indexRequest = instance.forEntity(ACTION_TYPE, RELATION_TYPE, ID);

    // verify
    assertThat(indexRequest, is(instanceOf(RelationIndexRequest.class)));
  }

  @Test
  public void forActionCreatesACollectionIndexRequestIfTheActionIsForMultipleEntities() {
    // action
    IndexRequest indexRequest = instance.forAction(Action.multiUpdateActionFor(TYPE));

    // verify
    assertThat(indexRequest, is(instanceOf(CollectionIndexRequest.class)));
  }

  @Test
  public void forActionCreatesAnEntityIndexRequestIfTheActionIsForASingleEntity() {
    // action
    IndexRequest indexRequest = instance.forAction(new Action(ActionType.MOD, TYPE, ID));

    // verify
    assertThat(indexRequest, is(instanceOf(EntityIndexRequest.class)));
  }

  @Test
  public void forActionCreatesARelationIndexRequestIfTheActionIsForASingleRelation() {
    // action
    IndexRequest indexRequest = instance.forAction(new Action(ActionType.MOD, RELATION_TYPE, ID));

    // verify
    assertThat(indexRequest, is(instanceOf(RelationIndexRequest.class)));
  }
}
