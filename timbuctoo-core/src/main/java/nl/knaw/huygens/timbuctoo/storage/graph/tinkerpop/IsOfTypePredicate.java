package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

public final class IsOfTypePredicate implements com.tinkerpop.blueprints.Predicate {
  @Override
  public boolean evaluate(Object first, Object second) {
    if (first != null && (first instanceof String)) {
      return ((String) first).contains((String) second);
    }
    return false;
  }
}