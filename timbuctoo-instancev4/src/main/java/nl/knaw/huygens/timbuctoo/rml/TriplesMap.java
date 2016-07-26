package nl.knaw.huygens.timbuctoo.rml;

public class TriplesMap {
  final SubjectMap subjectMap;
  final PredicateObjectMap[] predicateObjectMaps;
  LogicalSource logicalSource;

  public TriplesMap(SubjectMap subjectMap, PredicateObjectMap... predicateObjectMaps) {
    this.subjectMap = subjectMap;
    this.predicateObjectMaps = predicateObjectMaps;
  }
}
