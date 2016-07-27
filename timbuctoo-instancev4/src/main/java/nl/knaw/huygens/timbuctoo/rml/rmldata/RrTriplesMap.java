package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.ReferenceGetter;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RrTriplesMap {
  public final RrSubjectMap subjectMap;
  public final RrPredicateObjectMap[] predicateObjectMaps;
  public RrLogicalSource logicalSource;
  private List<ReferenceGetter> parentFields = new ArrayList<>();
  private List<ReferenceGetter> ownJoinFields = new ArrayList<>();

  public RrTriplesMap(RrLogicalSource logicalSource, RrSubjectMap subjectMap, RrPredicateObjectMap... predicateObjectMaps) {
    this.subjectMap = subjectMap;
    this.predicateObjectMaps = predicateObjectMaps;
    for (RrPredicateObjectMap predicateObjectMap : predicateObjectMaps) {
      if (predicateObjectMap.objectMap instanceof RrRefObjectMap) {
        final RrRefObjectMap rrRefObjectMap = (RrRefObjectMap) predicateObjectMap.objectMap;
        ownJoinFields.add(new ReferenceGetter(
          rrRefObjectMap.parentTriplesMap.logicalSource,
          rrRefObjectMap.rrJoinCondition.parent,
          rrRefObjectMap.rrJoinCondition.child,
          rrRefObjectMap.uniqueId
        ));
      }
    }

    this.logicalSource = logicalSource;
  }

  public void willBeUsedInJoinLaterOn(String parent, String child, String uniqueId) {
    parentFields.add(new ReferenceGetter(
      this.logicalSource,
      parent,
      child,
      uniqueId
    ));
  }

  public List<ReferenceGetter> getFieldsThatWillBeJoinedOn() {
    return parentFields;
  }

  public List<ReferenceGetter> getFieldsThatIamJoiningOn() {
    return ownJoinFields;
  }
}
