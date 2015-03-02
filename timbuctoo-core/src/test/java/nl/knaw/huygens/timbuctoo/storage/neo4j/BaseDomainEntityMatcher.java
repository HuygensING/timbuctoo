package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.BaseDomainEntity;

public class BaseDomainEntityMatcher<T extends BaseDomainEntity> extends CompositeMatcher<T> {
  private BaseDomainEntityMatcher() {}

  public static <U extends BaseDomainEntity> BaseDomainEntityMatcher<U> likeBaseDomainEntity(Class<U> type) {
    return new BaseDomainEntityMatcher<U>();
  }

  public BaseDomainEntityMatcher<T> withId(String id) {
    addMatcher(new PropertyEqualtityMatcher<T, String>("id", id) {

      @Override
      protected String getItemValue(T item) {
        return item.getId();
      }
    });

    return this;
  }

  public BaseDomainEntityMatcher<T> withACreatedValue() {
    addMatcher(new PropertyMatcher<T, Change>("created", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(T item) {
        return item.getCreated();
      }
    });
    return this;
  }

  public BaseDomainEntityMatcher<T> withAModifiedValue() {
    addMatcher(new PropertyMatcher<T, Change>("modified", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(T item) {
        return item.getModified();
      }
    });
    return this;
  }

  public BaseDomainEntityMatcher<T> withRevision(int revisionNumber) {
    addMatcher(new PropertyEqualtityMatcher<T, Integer>("rev", revisionNumber) {

      @Override
      protected Integer getItemValue(T item) {
        return item.getRev();
      }
    });
    return this;
  }

  public BaseDomainEntityMatcher<T> withAModifiedValueNotEqualTo(Change oldModified) {
    addMatcher(new PropertyMatcher<T, Change>("modified", not(equalTo(oldModified))) {

      @Override
      protected Change getItemValue(T item) {
        return item.getModified();
      }
    });
    return this;
  }
}
