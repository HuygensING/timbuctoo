package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Collection;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

public class PredicateMatcher extends CompositeMatcher<Predicate> {
  private PredicateMatcher() {

  }

  public static PredicateMatcher predicate() {
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

  public PredicateMatcher withWasList(Boolean wasList) {
    this.addMatcher(new PropertyEqualityMatcher<Predicate, Boolean>("list", wasList) {
      @Override
      protected Boolean getItemValue(Predicate item) {
        return item.hasBeenList();
      }
    });
    return this;
  }

  public PredicateMatcher withIsExplicit(Boolean isExplicit) {
    this.addMatcher(new PropertyEqualityMatcher<Predicate, Boolean>("isExplicit", isExplicit) {
      @Override
      protected Boolean getItemValue(Predicate item) {
        return item.isExplicit();
      }
    });
    return this;
  }

  public PredicateMatcher withReferenceType(String referenceType) {
    this.addMatcher(new PropertyMatcher<Predicate, Iterable<? super String>>("referenceTypes", hasItem(referenceType)) {
      @Override
      protected Iterable<String> getItemValue(Predicate item) {
        return item.getReferenceTypes().keySet();
      }
    });
    return this;
  }

  public PredicateMatcher withValueType(String valueType) {
    this.addMatcher(new PropertyMatcher<Predicate, Iterable<? super String>>("valueTypes", hasItem(valueType)) {
      @Override
      protected Iterable<String> getItemValue(Predicate item) {
        return item.getValueTypes().keySet();
      }
    });
    return this;
  }
}

