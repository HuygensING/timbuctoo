package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class TypeMatcher extends CompositeMatcher<Type> {

  private TypeMatcher() {

  }

  public static TypeMatcher type() {
    return new TypeMatcher();
  }

  public TypeMatcher withListPredicateWithName(String predicateName) {
    this.addMatcher(new PropertyMatcher<Type, Map<? extends String, ? extends Predicate>>(
      "predicates",
      hasEntry(is(predicateName), predicate().isList(true))
    ) {

      @Override
      protected Map<String, Predicate> getItemValue(Type item) {
        return item.getPredicates();
      }
    });

    return this;
  }

  public TypeMatcher withSinglePredicateWithName(String predicateName) {
    this.addMatcher(new PropertyMatcher<Type, Map<? extends String, ? extends Predicate>>(
      "predicates",
      hasEntry(is(predicateName), predicate().isList(false))
    ) {

      @Override
      protected Map<String, Predicate> getItemValue(Type item) {
        return item.getPredicates();
      }
    });

    return this;
  }

  private static PredicateMatcher predicate() {
    return new PredicateMatcher();
  }

  private static class PredicateMatcher extends CompositeMatcher<Predicate> {
    public PredicateMatcher isList(final boolean isList) {
      this.addMatcher(new PropertyEqualityMatcher<Predicate, Boolean>("list", isList) {
        @Override
        protected Boolean getItemValue(Predicate item) {
          return item.isList();
        }
      });
      return this;
    }
  }
}
