package nl.knaw.huygens.timbuctoo;

import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;

public class DerivedRelation extends Relation {
  private DerivedRelationDescription description;

  private DerivedRelation(){
    this.setTypeId("derived");
    this.setId("derived");
  }

  public static DerivedRelation aDerivedRelation(){
    return new DerivedRelation();
  }

  public DerivedRelation withDescription(DerivedRelationDescription description){
    this.description = description;
    return this;
  }

  public DerivedRelation withSource(String type, String id){
    this.setSourceType(type);
    this.setSourceId(id);

    return this;
  }

  public DerivedRelation withTarget(String type, String id){
    this.setTargetType(type);
    this.setTargetId(id);

    return this;
  }

  public RelationType getRelationType() {
    RelationType relationType = new RelationType();
    relationType.setTargetTypeName(this.getTargetType());
    relationType.setSourceTypeName(this.getSourceType());
    relationType.setRegularName(description.getDerivedTypeName());
    relationType.setId(this.getTypeId());

    return relationType;
  }
}
