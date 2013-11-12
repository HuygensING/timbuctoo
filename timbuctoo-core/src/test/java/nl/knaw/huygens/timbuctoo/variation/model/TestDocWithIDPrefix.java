package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix("TSTI")
public class TestDocWithIDPrefix extends GeneralTestDoc {

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TestDocWithIDPrefix { \ngeneralTestDocValue: ");
    sb.append(generalTestDocValue);
    sb.append("\nid: ");
    sb.append(getId());
    sb.append("\nroles: ");
    sb.append(getRoles());
    sb.append("\npid: ");
    sb.append(getPid());
    sb.append("\n}");

    return sb.toString();
  }
}
