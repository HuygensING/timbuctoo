package nl.knaw.huygens.timbuctoo.persistence.request;

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
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntityPersistenceRequestTest extends AbstractPersistenceRequestTest {

  public static final String ID = "id";
  public static final ProjectAPerson ENTITY = new ProjectAPerson();
  private EntityPersistenceRequest instance;
  private PersisterFactory persisterFactory;
  private Repository repository;

  @Before
  public void setUp() throws Exception {
    repository = mock(Repository.class);
    persisterFactory = mock(PersisterFactory.class);
    instance = new EntityPersistenceRequest(repository, persisterFactory, ACTION_TYPE, TYPE, ID);
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = instance.toAction();

    // verify
    assertThat(action, is(likeAction() //
      .withActionType(ACTION_TYPE) //
      .withType(TYPE) //
      .withId(ID)));
  }

  @Test
  public void executeTheCreatedPersiterForTheEntityOfTheRequest() {
    // setup
    when(repository.getEntityOrDefaultVariation(TYPE, ID)).thenReturn(ENTITY);

    Persister persister = mock(Persister.class);
    when(persisterFactory.forActionType(ACTION_TYPE)).thenReturn(persister);

    // action
    instance.execute();

    // verify
    verify(persister).execute(ENTITY);
  }

}
