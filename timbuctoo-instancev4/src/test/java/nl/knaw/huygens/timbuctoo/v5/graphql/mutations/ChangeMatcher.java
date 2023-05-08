package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change.Value;
import org.assertj.core.util.Lists;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.empty;

public class ChangeMatcher extends CompositeMatcher<Change> {
  private ChangeMatcher() {

  }

  public static ChangeMatcher likeChange() {
    return new ChangeMatcher();
  }

  public ChangeMatcher withSubject(String subject) {
    this.addMatcher(new PropertyEqualityMatcher<>("subject", subject) {
      @Override
      protected String getItemValue(Change item) {
        return item.getSubject();
      }
    });
    return this;
  }

  public ChangeMatcher withPredicate(String predicate) {
    this.addMatcher(new PropertyEqualityMatcher<>("predicate", predicate) {
      @Override
      protected String getItemValue(Change item) {
        return item.getPredicate();
      }
    });
    return this;
  }

  public ChangeMatcher withValues(Value... values) {
    this.addMatcher(new PropertyEqualityMatcher<Change, List<Value>>("values", Lists.newArrayList(values)) {
      @Override
      protected List<Value> getItemValue(Change item) {
        return item.getValues();
      }
    });
    return this;
  }

  public ChangeMatcher withOldValues(Value... oldValues) {
    this.addMatcher(new PropertyEqualityMatcher<Change, List<Value>>("oldValues", Lists.newArrayList(oldValues)) {
      @Override
      protected List<Value> getItemValue(Change item) {
        return item.getOldValues().collect(Collectors.toList());
      }
    });
    return this;
  }

  public ChangeMatcher valuesIsEmpty() {
    this.addMatcher(new PropertyMatcher<Change, Collection<? extends Value>>("values", empty()) {
      @Override
      protected Collection<Value> getItemValue(Change item) {
        return item.getValues();
      }
    });
    return this;
  }

  public ChangeMatcher oldValuesIsEmpty() {
    this.addMatcher(new PropertyEqualityMatcher<>("oldValues", false) {
      @Override
      protected Boolean getItemValue(Change item) {
        return item.getOldValues().findAny().isPresent();
      }
    });
    return this;
  }
}
