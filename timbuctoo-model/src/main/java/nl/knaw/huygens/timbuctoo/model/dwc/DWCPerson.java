package nl.knaw.huygens.timbuctoo.model.dwc;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;

public class DWCPerson extends Person {

  private String important;
  // later name omzetten naar PersonName
  private String family_name;
  private String given_name;
  private String preposition;
  private String intraposition;
  private String postposition;
  private String religion; // Zou een (relation) naar een Keyword kunnen worden?
  private String origin_db;
  private String data_line;

  public String getImportant() {
    return important;
  }

  public void setImportant(String important) {
    this.important = important;
  }

  public String getFamily_name() {
    return family_name;
  }

  public void setFamily_name(String family_name) {
    new PersonNameComponent(PersonNameComponent.Type.getInstance("surname"), family_name);
    this.family_name = family_name;
  }

  public String getGiven_name() {
    return given_name;
  }

  public void setGiven_name(String given_name) {
    this.given_name = given_name;
  }

  public String getPreposition() {
    return preposition;
  }

  public void setPreposition(String preposition) {
    this.preposition = preposition;
  }

  public String getIntraposition() {
    return intraposition;
  }

  public void setIntraposition(String intraposition) {
    this.intraposition = intraposition;
  }

  public String getPostposition() {
    return postposition;
  }

  public void setPostposition(String postposition) {
    this.postposition = postposition;
  }

  public void setGender(String gender) {
    //    System.err.println("gender: " + Gender.valueOf(gender) + " (" + Gender.valueOf(gender).getClass() + ")");
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

}
