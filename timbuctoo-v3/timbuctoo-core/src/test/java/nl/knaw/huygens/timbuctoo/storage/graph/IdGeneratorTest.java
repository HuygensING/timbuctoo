package nl.knaw.huygens.timbuctoo.storage.graph;

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
