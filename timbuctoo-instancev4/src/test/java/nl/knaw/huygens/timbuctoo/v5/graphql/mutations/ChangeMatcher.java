package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import co.unruly.matchers.StreamMatchers;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change.Value;
import org.assertj.core.util.Lists;

import java.util.List;
import java.util.stream.Stream;

public class ChangeMatcher extends CompositeMatcher<Change> {
  private ChangeMatcher() {

  }

  public static ChangeMatcher likeChange() {
    return new ChangeMatcher();
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
    this.addMatcher(new PropertyMatcher<Change, Stream<Value>>("oldValues", StreamMatchers.contains(oldValues)) {
      @Override
      protected Stream<Value> getItemValue(Change item) {
        return item.getOldValues();
      }
    });
    return this;
  }

  public ChangeMatcher valuesIsEmpty() {
    this.addMatcher(new PropertyEqualityMatcher<Change, Integer>("values", 0) {
      @Override
      protected Integer getItemValue(Change item) {
        return item.getValues().size();
      }
    });
    return this;
  }

  public ChangeMatcher oldValuesIsEmpty() {
    this.addMatcher(new PropertyEqualityMatcher<Change, Long>("oldValues", 0L) {
      @Override
      protected Long getItemValue(Change item) {
        return item.getOldValues().count();
      }
    });
    return this;
  }
}
