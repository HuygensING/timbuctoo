package nl.knaw.huygens.timbuctoo.storage.graph;

import com.google.common.collect.Lists;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.hamcrest.ListContainsItemsInAnyOrderMatcher.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class DomainEntityMatcher<T extends DomainEntity> extends CompositeMatcher<T> {
  private DomainEntityMatcher() {
  }

  public static <U extends DomainEntity> DomainEntityMatcher<U> likeDomainEntity(Class<U> type) {
    return new DomainEntityMatcher<U>();
  }

  public DomainEntityMatcher<T> withId(String id) {
    addMatcher(new PropertyEqualityMatcher<T, String>("id", id) {

      @Override
      protected String getItemValue(T item) {
        return item.getId();
      }
    });

    return this;
  }

  public DomainEntityMatcher<T> withACreatedValue() {
    addMatcher(new PropertyMatcher<T, Change>("created", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(T item) {
        return item.getCreated();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withAModifiedValue() {
    addMatcher(new PropertyMatcher<T, Change>("modified", notNullValue(Change.class)) {

      @Override
      protected Change getItemValue(T item) {
        return item.getModified();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withRevision(int revisionNumber) {
    addMatcher(new PropertyEqualityMatcher<T, Integer>("rev", revisionNumber) {

      @Override
      protected Integer getItemValue(T item) {
        return item.getRev();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withAModifiedValueNotEqualTo(Change oldModified) {
    addMatcher(new PropertyMatcher<T, Change>("modified", not(equalTo(oldModified))) {

      @Override
      protected Change getItemValue(T item) {
        return item.getModified();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withoutAPID() {
    addMatcher(new PropertyMatcher<T, String>("pid", nullValue(String.class)) {

      @Override
      protected String getItemValue(T item) {
        return item.getPid();
      }
    });

    return this;
  }

  public DomainEntityMatcher<T> withPID(String pid) {
    addMatcher(new PropertyEqualityMatcher<T, String>("pid", pid) {

      @Override
      protected String getItemValue(T item) {
        return item.getPid();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withVariations(Class<?> type, Class<?>... types) {
    List<String> variations = Lists.newArrayList(getInternalName(type));

    if (types != null) {
      for (Class<?> typeInTypes : types) {
        variations.add(getInternalName(typeInTypes));
      }
    }

    this.addMatcher(new PropertyMatcher<T, List<String>>("variations", containsInAnyOrder(variations)) {

      @Override
      protected List<String> getItemValue(T item) {
        return item.getVariations();
      }
    });
    return this;
  }


  public DomainEntityMatcher<T> withCreated(Change created) {
    this.addMatcher(new PropertyEqualityMatcher<T, Change>("created", created) {
      @Override
      protected Change getItemValue(T item) {
        return item.getCreated();
      }
    });
    return this;
  }

  public DomainEntityMatcher<T> withModified(Change modified) {
    this.addMatcher(new PropertyEqualityMatcher<T, Change>("modified", modified) {
      @Override
      protected Change getItemValue(T item) {
        return item.getModified();
      }
    });
    return this;
  }
}
