package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

public final class IsOfTypePredicate implements com.tinkerpop.blueprints.Predicate {
  @Override
  public boolean evaluate(Object object, Object shouldApplyTo) {
    if (object != null && (object instanceof String)) {

      return ((String) object).matches(String.format("(.+\\W)?%s(\\W.+)?", shouldApplyTo));
    }
    return false;
  }
}
