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
import nl.knaw.huygens.timbuctoo.messages.Action;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class EntityIndexRequestTest extends AbstractIndexRequestTest {

  private static final String ID = "id";

  @Override
  protected IndexRequest createInstance() {
    return new EntityIndexRequest(getIndexerFactory(), ACTION_TYPE, TYPE, ID);
  }


  @Test
  public void executeLetsTheIndexerExecuteAnIndexAction() throws Exception {
    // action
    getInstance().execute();

    // verify
    verify(getIndexer()).executeIndexAction(TYPE, ID);
  }

  @Test(expected = IndexException.class)
  public void executeThrowsAnIndexExceptionWhenTheIndexerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(getIndexer()).executeIndexAction(TYPE, ID);

    // action
    getInstance().execute();
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = getInstance().toAction();

    // verify
    assertThat(action, likeAction()//
      .withActionType(ACTION_TYPE) //
      .withType(TYPE) //
      .withId(ID));
  }
}
