package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Watermerk;

public class EBNMWatermerk extends Watermerk {

  private String watermerkId;
  private String watermerk;

  public String getLabel() {
    return getValue();
  }

  public String getCodeId() {
    return watermerkId;
  }

  public void setCodeId(String _id) {
    this.watermerkId = _id;
  }

  public String getWatermerk() {
    return watermerk;
  }

  public void setWatermerk(String watermerk) {
    this.watermerk = watermerk;
  }

}
