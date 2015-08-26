package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class IsOfTypePredicateTest {

  private IsOfTypePredicate instance;

  @Before
  public void setup(){
    instance = new IsOfTypePredicate();
  }

  @Test
  public void evaluateReturnsTrueIfTheStringsAreEqual(){
    // action
    boolean result = instance.evaluate("[\"test\"]", "test");

    //verify
    assertThat(result, is(true));
  }



  @Test
  public void evaluateReturnsTrueIfFirstStringStartsWithTheSecond(){
    // action
    boolean result = instance.evaluate("[\"test\",\"1234\",\"blash\"", "test");

    //verify
    assertThat(result, is(true));
  }

  @Test
  public void evaluateReturnsTrueIfTheFirstStringEndsWithTheSecond(){
    // action
    boolean result = instance.evaluate("[\"1234\",\"blash\",\"test\"]", "test");

    //verify
    assertThat(result, is(true));
  }

  @Test
  public void evaluateReturnsTrueWhenTheFirstContainsTheSecondInTheMiddle(){
    // action
    boolean result = instance.evaluate("[\"1234\",\"blash\",\"test\",\"1235\"]", "test");

    //verify
    assertThat(result, is(true));
  }

  @Test
  public void evaluateReturnsFalseWhenTheFirstStartsWithTheSecondButContainsSomeWordCharactersDirectlyAfterIt(){
    // action
    boolean result = instance.evaluate("[\"testabc\",\"1234\",\"blash\"]", "test");

    //verify
    assertThat(result, is(false));
  }

  @Test
  public void evaluateReturnsFalseWhenTheFirstEndsOnTheSecondButContainsSomeWordCharactersDirectlyBeforeIt(){
    // action
    boolean result = instance.evaluate("[\"1234\",\"blashtest\"]", "test");

    //verify
    assertThat(result, is(false));
  }

  @Test
  public void evaluateReturnsFalseWhenTheFirstContainsTheSecondButContainsSomeWordCharactersDirectlyAfterIt(){
    // action
    boolean result = instance.evaluate("[\"1234\",\"blash\",\"testasdf\",\"1235\"]", "test");

    //verify
    assertThat(result, is(false));
  }

  @Test
  public void evaluateReturnsFalseWhenTheFirstContainsTheSecondButContainsSomeWordCharactersDirectlyBeforeIt(){
    // action
    boolean result = instance.evaluate("[\"1234\",\"blash\",\"dsfdstest\",\"1235\"]", "test");

    //verify
    assertThat(result, is(false));
  }

}
