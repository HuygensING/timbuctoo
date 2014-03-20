package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Taal;

public class EBNMTaal extends Taal {

  private String taalId;
  private String taal;

  public String getLabel() {
    return getValue();
  }

  public void setTaalId(String taal_id) {
    this.taalId = taal_id;
  }

  String getTaalId() {
    return taalId;
  }

  public void setTaal(String taal) {
    this.taal = taal;
  }

  String getTaal() {
    return taal;
  }

}
