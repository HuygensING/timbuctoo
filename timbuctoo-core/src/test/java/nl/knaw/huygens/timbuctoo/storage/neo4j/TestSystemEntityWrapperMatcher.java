package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.TestSystemEntityWrapper;

public class TestSystemEntityWrapperMatcher extends CompositeMatcher<TestSystemEntityWrapper> {
  private TestSystemEntityWrapperMatcher() {}

  public static TestSystemEntityWrapperMatcher likeTestSystemEntityWrapper() {
    return new TestSystemEntityWrapperMatcher();
  }

  public TestSystemEntityWrapperMatcher with(String id) {
    addMatcher(new PropertyEqualtityMatcher<TestSystemEntityWrapper, String>("id", id) {

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

  public TestSystemEntityWrapperMatcher withRevision(int revisionNumber) {
    addMatcher(new PropertyEqualtityMatcher<TestSystemEntityWrapper, Integer>("rev", revisionNumber) {

      @Override
      protected Integer getItemValue(TestSystemEntityWrapper item) {
        return item.getRev();
      }
    });
    return this;
  }
}
