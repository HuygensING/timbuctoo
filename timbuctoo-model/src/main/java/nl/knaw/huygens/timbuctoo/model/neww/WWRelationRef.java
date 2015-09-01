package nl.knaw.huygens.timbuctoo.model.neww;

import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class WWRelationRef extends RelationRef {

  private Datable date;

  public WWRelationRef() {

  }

  public WWRelationRef(String type, String xtype, String id, String displayName, String relationId, boolean accepted, int rev, String relationName, Datable date) {
    super(type, xtype, id, displayName, relationId, accepted, rev, relationName);
    this.date = date;
  }

  public Datable getDate() {
    return date;
  }

  public void setDate(Datable date) {
    this.date = date;
  }

}
