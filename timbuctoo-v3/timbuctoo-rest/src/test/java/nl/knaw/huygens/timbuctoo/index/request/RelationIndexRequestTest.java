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
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectAPerson;
import test.rest.model.projecta.ProjectARelation;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RelationIndexRequestTest extends AbstractIndexRequestTest {

  private static final String ID = "id";
  private static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private static final Class<ProjectADomainEntity> SOURCE_TYPE = ProjectADomainEntity.class;
  private static final String SOURCE_ID = "sourceId";
  private static final Class<ProjectAPerson> TARGET_TYPE = ProjectAPerson.class;
  private static final String TARGET_ID = "targetId";

  @Override
  protected IndexRequest createInstance() {
    return new RelationIndexRequest(getIndexerFactory(), createRepository(), createTypeRegistry(), ACTION_TYPE, RELATION_TYPE, ID);
  }

  private TypeRegistry createTypeRegistry() {
    TypeRegistry instance = TypeRegistry.getInstance();
    try {
      instance.init(RELATION_TYPE.getPackage().getName());
    } catch (ModelException e) {
      throw new RuntimeException(e);
    }
    return instance;
  }

  private Repository createRepository() {
    Repository repository = mock(Repository.class);

    ProjectARelation projectARelation = new ProjectARelation(ID, getInternalName(SOURCE_TYPE), SOURCE_ID, getInternalName(TARGET_TYPE), TARGET_ID);

    when(repository.getEntityOrDefaultVariation(Relation.class, ID)).thenReturn(projectARelation);

    return repository;
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = getInstance().toAction();

    // verify
    assertThat(action, likeAction()//
      .withActionType(ACTION_TYPE) //
      .withType(RELATION_TYPE) //
      .withId(ID));
  }

  @Test
  public void executeExecutesTheCreatedIndexerForTheRelationThanExecutesAModIndexerForTheSourceAndTarget() throws Exception {
    // setup
    Indexer modIndexer = setupModIndexer();

    // action
    getInstance().execute();

    // verify
    verify(getIndexerFactory()).create(ACTION_TYPE);
    verify(getIndexer()).executeIndexAction(RELATION_TYPE, ID);
    verify(getIndexerFactory()).create(ActionType.MOD);
    verify(modIndexer).executeIndexAction(SOURCE_TYPE, SOURCE_ID);
    verify(modIndexer).executeIndexAction(TARGET_TYPE, TARGET_ID);
  }

  private Indexer setupModIndexer() {
    Indexer modIndexer = mock(Indexer.class);
    when(getIndexerFactory().create(ActionType.MOD)).thenReturn(modIndexer);
    when(getIndexerFactory().create(ACTION_TYPE)).thenReturn(getIndexer());
    return modIndexer;
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void executeDoesNotIndexTheSourceAndTargetWhenTheIndexOfTheRelationThrowsAnIndexException() throws Exception {
    // setup
    Indexer modIndexer = setupModIndexer();
    doThrow(IndexException.class).when(getIndexer()).executeIndexAction(RELATION_TYPE, ID);

    // setup expected exception
    expectedException.expect(IndexException.class);


    // action
    getInstance().execute();

    // verify
    verify(getIndexer()).executeIndexAction(RELATION_TYPE, ID);
    verifyZeroInteractions(modIndexer);
  }

}
