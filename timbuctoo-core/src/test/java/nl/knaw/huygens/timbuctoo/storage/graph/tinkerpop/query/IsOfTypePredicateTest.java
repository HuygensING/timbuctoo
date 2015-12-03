package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

/*
 * #%L
 * Timbuctoo core
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
