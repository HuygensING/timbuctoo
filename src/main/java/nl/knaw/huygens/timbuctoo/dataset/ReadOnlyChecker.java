package nl.knaw.huygens.timbuctoo.dataset;

public interface ReadOnlyChecker {
  boolean isReadonlyPredicate(String predicateIri);

  boolean isReadonlyType(String typeUri);
}
