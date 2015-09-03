package nl.knaw.huygens.timbuctoo.index.request;

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
