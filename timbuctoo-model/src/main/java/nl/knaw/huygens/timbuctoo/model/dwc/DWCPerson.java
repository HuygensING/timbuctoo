package nl.knaw.huygens.timbuctoo.model.dwc;

import nl.knaw.huygens.timbuctoo.model.Person;

public class DWCPerson extends Person {

  private String important;
  private String religion; // Zou een (relation) naar een Keyword kunnen worden? Nee!!!
  private String origin_db;
  private String data_line;
  private String scientist_bio;

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

  public String getOrigin_db() {
    return origin_db;
  }

  public void setOrigin_db(String origin_db) {
    this.origin_db = origin_db;
  }

  public String getData_line() {
    return data_line;
  }

  public void setData_line(String data_line) {
    this.data_line = data_line;
  }

  public String getScientist_bio() {
    return scientist_bio;
  }

  public void setScientist_bio(String scientist_bio) {
    this.scientist_bio = scientist_bio;
  }

}
