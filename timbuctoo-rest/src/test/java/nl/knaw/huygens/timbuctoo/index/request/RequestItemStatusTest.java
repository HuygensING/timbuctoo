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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

public class RequestItemStatusTest {

  private static final String ID_1 = "id1";
  private static final String ID_2 = "id2";
  private static final List<String> TO_DO = Lists.newArrayList(ID_1, ID_2);
  private static final String ID_NOT_IN_TO_DO = "idNotInToDo";
  private RequestItemStatus instance;

  @Before
  public void setUp() throws Exception {
    instance = new RequestItemStatus();
  }

  @Test
  public void setToDoSetsTheIdsOfTheItemsThatHaveToBeProcessed() {
    // action
    instance.setToDo(TO_DO);

    // verify
    assertThat(instance.getToDo(), is(TO_DO));
  }

  @Test
  public void doneMovesAnItemFromTheToDoListToTheDoneList() {
    // setup
    instance.setToDo(TO_DO);

    // action
    instance.done(ID_1);

    // verify
    assertThat(instance.getToDo(), contains(ID_2));
    assertThat(instance.getDone(), contains(ID_1));
  }


  @Test
  public void doneDoesNotAddTheIdToDoneWhenTheItemWasNotRemovedFromToDo() {
    // setup
    instance.setToDo(TO_DO);

    // action
    instance.done(ID_NOT_IN_TO_DO);

    // verify
    assertThat(instance.getToDo(), containsInAnyOrder(ID_1, ID_2));
    assertThat(instance.getDone(), is(empty()));
  }

  @Test
  public void toDoCannotBeManipulatedFromTheOutside() {
    // setup
    instance.setToDo(TO_DO);
    List<String> toDo = instance.getToDo();

    // action
    toDo.remove(ID_1);

    // verify
    assertThat(instance.getToDo(), containsInAnyOrder(ID_1, ID_2));

  }

  @Test
  public void doneCannotBeManipulatedFromTheOutside() {
    // setup
    List<String> done = instance.getDone();

    // action
    done.add(ID_NOT_IN_TO_DO);

    // verify
    assertThat(instance.getDone(), is(empty()));
  }

}
