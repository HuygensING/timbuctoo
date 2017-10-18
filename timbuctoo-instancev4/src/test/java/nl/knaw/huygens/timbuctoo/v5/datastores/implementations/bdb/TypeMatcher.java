package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import org.hamcrest.Matchers;

import java.util.Collection;

import static org.hamcrest.Matchers.hasItem;

public class TypeMatcher extends CompositeMatcher<Type> {

  private TypeMatcher() {

  }

  public static TypeMatcher type() {
    return new TypeMatcher();
  }

  public TypeMatcher withListPredicateWithName(String predicateName) {
    this.addMatcher(new PropertyMatcher<Type, Iterable<? super Predicate>>(
      "predicates",
      Matchers.hasItem(predicate().hasName(predicateName).isList(true))
    ) {

      @Override
      protected Collection<Predicate> getItemValue(Type item) {
        return item.getPredicates();
      }
    });

    return this;
  }

  public TypeMatcher withSinglePredicateWithName(String predicateName) {
    this.addMatcher(new PropertyMatcher<Type, Iterable<? super Predicate>>(
      "predicates",
      hasItem(predicate().hasName(predicateName).isList(false))
    ) {

      @Override
      protected Collection<Predicate> getItemValue(Type item) {
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


    public PredicateMatcher hasName(final String name) {
      this.addMatcher(new PropertyEqualityMatcher<Predicate, String>("name", name) {
        @Override
        protected String getItemValue(Predicate item) {
          return item.getName();
        }
      });
      return this;
    }
  }
}
