package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

public class RelationClientSearchResult extends ClientSearchResult {
  private List<ClientRelationRepresentation> refs;

  public List<ClientRelationRepresentation> getRefs() {
    return refs;
  }

  public void setRefs(List<ClientRelationRepresentation> refs) {
    this.refs = refs;
  }

}
