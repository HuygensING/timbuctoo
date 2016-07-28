package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.ReferenceGetter;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;

import java.util.ArrayList;
import java.util.List;

public class RrTriplesMap {
  private RrSubjectMap subjectMap;
  private List<RrPredicateObjectMap> predicateObjectMaps = new ArrayList<>();
  private RrLogicalSource logicalSource;
  private final List<ReferenceGetter> parentFields = new ArrayList<>();
  private final List<ReferenceGetter> ownJoinFields = new ArrayList<>();

  public RrTriplesMap() {
  }

  void addPredicateObjectMap(RrPredicateObjectMap map) {
    this.predicateObjectMaps.add(map);
    if (map.getObjectMap() instanceof RrRefObjectMap) {
      final RrRefObjectMap rrRefObjectMap = (RrRefObjectMap) map.getObjectMap();
      ownJoinFields.add(new ReferenceGetter(
        rrRefObjectMap.getParentTriplesMap().logicalSource,
        rrRefObjectMap.getRrJoinCondition().getParent(),
        rrRefObjectMap.getRrJoinCondition().getChild(),
        rrRefObjectMap.getUniqueId()
      ));
    }
  }

  void addParentJoinField(ReferenceGetter getter) {
    parentFields.add(getter);
  }

  public List<ReferenceGetter> getFieldsThatWillBeJoinedOn() {
    return parentFields;
  }

  public List<ReferenceGetter> getFieldsThatIamJoiningOn() {
    return ownJoinFields;
  }

  public RrSubjectMap getSubjectMap() {
    return subjectMap;
  }

  public List<RrPredicateObjectMap> getPredicateObjectMaps() {
    return predicateObjectMaps;
  }

  public RrLogicalSource getLogicalSource() {
    return logicalSource;
  }

  public static Builder rrTriplesMap() {
    return new Builder();
  }

  public static class Builder {
    private final RrTriplesMap instance;
    private RrLogicalSource.Builder logicalSourceBuilder;
    private RrSubjectMap.Builder subjectMapBuilder;
    private List<RrPredicateObjectMap.Builder> predicateObjectMapBuilders = new ArrayList<>();

    Builder() {
      this.instance = new RrTriplesMap();
    }

    public Builder withLogicalSource(RrLogicalSource.Builder subBuilder) {
      this.logicalSourceBuilder = subBuilder;
      return this;
    }

    public RrLogicalSource.Builder withLogicalSource() {
      this.logicalSourceBuilder = new RrLogicalSource.Builder();
      return logicalSourceBuilder;
    }

    public Builder withSubjectMap(RrSubjectMap.Builder subBuilder) {
      this.subjectMapBuilder = subBuilder;
      return this;
    }

    public RrSubjectMap.Builder withSubjectMap() {
      this.subjectMapBuilder = new RrSubjectMap.Builder();
      return this.subjectMapBuilder;
    }

    public Builder withPredicateObjectMap(RrPredicateObjectMap.Builder subBuilder) {
      this.predicateObjectMapBuilders.add(subBuilder);
      return this;
    }

    public RrPredicateObjectMap.Builder withPredicateObjectMap() {
      final RrPredicateObjectMap.Builder subBuilder = new RrPredicateObjectMap.Builder();
      this.predicateObjectMapBuilders.add(subBuilder);
      return subBuilder;
    }

    RrTriplesMap build() {
      instance.logicalSource = logicalSourceBuilder.build();
      instance.subjectMap = subjectMapBuilder.build();
      for (RrPredicateObjectMap.Builder subBuilder : this.predicateObjectMapBuilders) {
        instance.addPredicateObjectMap(subBuilder.build(this.instance));
      }
      return instance;
    }
  }
}
