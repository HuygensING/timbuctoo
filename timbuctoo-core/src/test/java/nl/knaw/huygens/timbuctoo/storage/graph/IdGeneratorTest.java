package nl.knaw.huygens.timbuctoo.storage.graph;

import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IdGeneratorTest {

  private IdGenerator instance;

  @Before
  public void setUp() {
    instance = new IdGenerator();
  }

  @Test
  public void nextIdForCreatesANewIdEachTimeItIsCalled() {
    // setup
    Class<TestSystemEntityWrapper> type = TestSystemEntityWrapper.class;

    // action
    String id1 = instance.nextIdFor(type);
    String id2 = instance.nextIdFor(type);

    // verify
    assertThat(id1, is(not(equalTo(id2))));
  }
}
