package nl.knaw.huygens.timbuctoo.model.dwc;

import nl.knaw.huygens.timbuctoo.model.Person;

public class DWCPerson extends Person {

  private String important;
  private String religion; // Zou een (relation) naar een Keyword kunnen worden? Nee!!!
  private String originDb;
  private String dataLine;
  private String scientistBio;

  public String getImportant() {
    return important;
  }

  public void setImportant(String important) {
    this.important = important;
  }

  public void setGender(String gender) {
    setGender(Gender.valueOf(gender));
  }

  public String getReligion() {
    return religion;
  }

  public void setReligion(String religion) {
    this.religion = religion;
  }

  public String getOriginDb() {
    return originDb;
  }

  public void setOriginDb(String origin_db) {
    this.originDb = origin_db;
  }

  public String getDataLine() {
    return dataLine;
  }

  public void setDataLine(String data_line) {
    this.dataLine = data_line;
  }

  public String getScientistBio() {
    return scientistBio;
  }

  public void setScientistBio(String scientist_bio) {
    this.scientistBio = scientist_bio;
  }

}
