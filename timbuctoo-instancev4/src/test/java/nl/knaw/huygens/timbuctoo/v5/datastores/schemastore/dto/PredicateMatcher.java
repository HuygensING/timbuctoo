package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

public class PredicateMatcher extends CompositeMatcher<Predicate> {
  private PredicateMatcher() {

  }

  public static PredicateMatcher predicateMatcher() {
    return new PredicateMatcher();
  }

  public PredicateMatcher withName(String name) {
    this.addMatcher(new PropertyEqualityMatcher<Predicate, String>("name", name) {
      @Override
      protected String getItemValue(Predicate item) {
        return item.getName();
      }
    });
    return this;
  }

  public PredicateMatcher withDirection(Direction direction) {
    this.addMatcher(new PropertyEqualityMatcher<Predicate, Direction>("direction", direction) {
      @Override
      protected Direction getItemValue(Predicate item) {
        return item.getDirection();
      }
    });
    return this;
  }
}
