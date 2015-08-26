package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

public final class IsOfTypePredicate implements com.tinkerpop.blueprints.Predicate {

  private static final String FORMAT = "(?:.+\\W)?%s(?:\\W.+)?";

  @Override
  public boolean evaluate(Object object, Object shouldApplyTo) {
    if (object != null && (object instanceof String)) {
      return ((String) object).matches(String.format(FORMAT, shouldApplyTo));
    }
    return false;
  }
}
