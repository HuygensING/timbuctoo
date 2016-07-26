package nl.knaw.huygens.timbuctoo.rml;

public class PredicateObjectMap {
  final String predicate;
  final TermMapContent objectMap;

  public PredicateObjectMap(String predicate, TermMapContent objectMap) {

    this.predicate = predicate;
    this.objectMap = objectMap;
  }
}
