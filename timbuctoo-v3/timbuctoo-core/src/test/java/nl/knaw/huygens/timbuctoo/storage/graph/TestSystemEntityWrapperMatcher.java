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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.TestSystemEntityWrapper;

public class TestSystemEntityWrapperMatcher extends CompositeMatcher<TestSystemEntityWrapper> {
  private TestSystemEntityWrapperMatcher() {}

  public static TestSystemEntityWrapperMatcher likeTestSystemEntityWrapper() {
    return new TestSystemEntityWrapperMatcher();
  }

  public TestSystemEntityWrapperMatcher withId(String id) {
    addMatcher(new PropertyEqualityMatcher<TestSystemEntityWrapper, String>("id", id) {

      @Override
      protected String getItemValue(TestSystemEntityWrapper item) {
        return item.getId();
      }
    });

    return this;
  }

  public TestSystemEntityWrapperMatcher withACreatedValue() {
    addMatcher(new PropertyMatcher<TestSystemEntityWrapper, Change>("created", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(TestSystemEntityWrapper item) {
        return item.getCreated();
      }
    });
    return this;
  }

  public TestSystemEntityWrapperMatcher withAModifiedValue() {
    addMatcher(new PropertyMatcher<TestSystemEntityWrapper, Change>("modified", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(TestSystemEntityWrapper item) {
        return item.getModified();
      }
    });
    return this;
  }

  public TestSystemEntityWrapperMatcher withAModifiedValueNotEqualTo(Change oldModified) {
    addMatcher(new PropertyMatcher<TestSystemEntityWrapper, Change>("modified", not(equalTo(oldModified))) {

      @Override
      protected Change getItemValue(TestSystemEntityWrapper item) {
        return item.getModified();
      }
    });
    return this;
  }

  public TestSystemEntityWrapperMatcher withRevision(int revisionNumber) {
    addMatcher(new PropertyEqualityMatcher<TestSystemEntityWrapper, Integer>("rev", revisionNumber) {

      @Override
      protected Integer getItemValue(TestSystemEntityWrapper item) {
        return item.getRev();
      }
    });
    return this;
  }

}
